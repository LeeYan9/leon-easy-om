package com.lyon.easy.async.task.core;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.date.SystemClock;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.lyon.easy.async.task.config.ExecutorConfig;
import com.lyon.easy.async.task.config.TaskGroupConfig;
import com.lyon.easy.async.task.converter.BatchTaskConverter;
import com.lyon.easy.async.task.converter.SubTaskConverter;
import com.lyon.easy.async.task.core.idc.IdcContainer;
import com.lyon.easy.async.task.dal.dataobject.task.BatchTaskDO;
import com.lyon.easy.async.task.dal.dataobject.task.SubTaskDO;
import com.lyon.easy.async.task.dal.mysql.task.BatchSubTaskMapper;
import com.lyon.easy.async.task.dal.mysql.task.BatchTaskMapper;
import com.lyon.easy.async.task.data.BatchTask;
import com.lyon.easy.async.task.data.SubTask;
import com.lyon.easy.async.task.enums.ExecStatus;
import com.lyon.easy.async.task.enums.IdcEnum;
import com.lyon.easy.async.task.factory.TaskHandlerFactory;
import com.lyon.easy.async.task.handler.BatchTaskHandler;
import com.lyon.easy.async.task.util.ParamsCheckerUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.lyon.easy.common.utils.CollUtils.toNonEmptyList;

/**
 * @author Lyon
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
@Slf4j
public class BatchTaskManager implements ApplicationContextAware, InitializingBean {

    @Getter
    private final ExecutorConfig executorConfig;

    private final String tablePrefix;

    private final MachineId machineId;

    private final ExecutorManager executorManager;

    private final TaskHandlerFactory taskHandlerFactory;

    private final IdcContainer idcContainer;

    private final AcquireTaskService acquireTaskService;

    private final BatchTaskHeartBeatReactor heartBeatReactor;

    @Resource
    @Getter
    private BatchTaskMapper batchTaskMapper;

    @Resource
    @Getter
    private BatchSubTaskMapper batchSubTaskMapper;

    private ApplicationContext ac;

    public BatchTaskManager(ExecutorConfig executorConfig,
                            IdcContainer idcContainer,
                            TaskHandlerFactory taskHandlerFactory) {
        this.executorConfig = executorConfig;
        this.idcContainer = idcContainer;
        this.taskHandlerFactory = taskHandlerFactory;
        this.machineId = new MachineId(idcContainer);
        ParamsCheckerUtil.check(this.executorConfig);
        ParamsCheckerUtil.check(this);
        this.executorManager = new ExecutorManager(executorConfig);
        this.acquireTaskService = new AcquireTaskService(this);
        this.heartBeatReactor = new BatchTaskHeartBeatReactor(this);
        this.tablePrefix = "";
    }

    public void init() {
        this.machineId.init();
        this.executorConfig
                .getTaskGroupConfigs()
                .forEach(taskGroupConfig ->
                        taskGroupConfig.setExecutorId(String.format("%s-%s", machineId.getId(), taskGroupConfig.getName())));
        this.acquireTaskService.init();
        this.heartBeatReactor.init();
    }

    public void atOnceExecute(String group) {
        acquireTaskService.atOnceAcquire(group);
    }

    public List<SubTaskDO> determineTasksOfExec(TaskGroupConfig taskGroupConfig) {
        // FIXME 有微小的概率，集群机器同时扫描到相同的子列表，优化？
        return batchSubTaskMapper.chooseSubTask(idcContainer.idc(), taskGroupConfig.getGroups(), taskGroupConfig.getTaskLimit());
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitTask(BatchTask batchTask) {
        // write db
        ParamsCheckerUtil.check(batchTask);
        log.info("try add batch task start [{}]-[{}]-[{}]-[{}]",
                batchTask.getBatchNo(), batchTask.getGroupName(), batchTask.getJobName(),
                batchTask.getIdcType());
        // save batch task
        final BatchTaskDO batchTaskDO = BatchTaskConverter.INSTANCE.from(batchTask);
        batchTaskMapper.insert(batchTaskDO);
        // save sub task list
        final List<SubTaskDO> listAll = Lists.newArrayList();
        batchTask.getSubTasks().forEach(subTask -> listAll.addAll(generateSubTaskDOList(batchTaskDO, subTask)));
        listAll.forEach(subTask -> batchSubTaskMapper.insert(subTask));
    }

    private List<SubTaskDO> generateSubTaskDOList(BatchTaskDO batchTaskDO, SubTask subTask) {
        List<SubTaskDO> subTaskDOList;
        if (IdcEnum.ALL == batchTaskDO.getIdcType()) {
            subTaskDOList = toNonEmptyList(idcContainer.idcList(), idc -> SubTaskConverter.INSTANCE.from(batchTaskDO, subTask, idc));
        } else {
            subTaskDOList = List.of(SubTaskConverter.INSTANCE.from(subTask, batchTaskDO));
        }
        subTaskDOList.forEach(subTaskDO -> log.info("try add sub task [{}]-[{}]-[idc:{}]-[{}]",
                batchTaskDO.getBatchNo(), subTaskDO.getJobNo(), subTaskDO.getIdc(), subTaskDO.getTaskAddress()));
        return subTaskDOList;
    }

    public void kernelExecTask(TaskGroupConfig taskGroupConfig, SubTaskDO subTaskDO) {
        final TaskWorkRunnable worker = new TaskWorkRunnable(taskGroupConfig, subTaskDO);
        executorManager.getExecutor(taskGroupConfig.getName()).submit(worker);
    }

    public int releaseLockWhenHeartBeatExpireIn() {
        long heartBeatCheckerTime = executorConfig.getHearBeatTimeMills() * executorConfig.getHearBeatTimeoutInterval();
        final LocalDateTime minHeartBeatTime = LocalDateTimeUtil.of(SystemClock.now() - heartBeatCheckerTime);
        return batchSubTaskMapper.releaseLockWhenHeartBeatExpireIn(minHeartBeatTime);
    }

    public int renewHeartbeat() {
        return batchSubTaskMapper.renewHeartbeat(machineId.getId());
    }

    @Override
    public void setApplicationContext(@SuppressWarnings("NullableProblems") ApplicationContext ac) throws BeansException {
        this.ac = ac;
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }


    @RequiredArgsConstructor
    @Getter
    class TaskWorkRunnable implements Runnable {

        private final TaskGroupConfig taskGroupConfig;

        private final SubTaskDO subTaskDO;

        private int maxFailureCnt;

        private String executorId;

        @Override
        public void run() {
            executorId = taskGroupConfig.getExecutorId();
            maxFailureCnt = ObjectUtil.defaultIfNull(taskGroupConfig.getMaxFailureCount(), executorConfig.getMaxFailureCount());
            // real exec task
            // add table#sit_row && row-record add mutex lock
            ExecResult<?> execResult;
            try {
                log.info("[task-executor] [{}] add lock jobNo [{}] ", executorId, subTaskDO.getJobNo());
                // 加锁
                if (!acquireLock()) {
                    log.info("[task-executor] [{}] add lock failed jobNo [{}] ", executorId, subTaskDO.getJobNo());
                    return;
                }
                // 调用
                log.info("[task-executor] [{}] add lock successful jobNo [{}] ", executorId, subTaskDO.getJobNo());
                execResult = execute();
            } catch (Exception e) {
                execResult = ExecResult.error(ExecStatus.FAILED, e);
                log.error("[task-executor] error [{}] details[{}]", executorId, JSONUtil.toJsonStr(subTaskDO), e);
            }
            try {
                // FIXME  可以尝试注册结果回调，链式执行
                // You can try registering the result callback, chaining execution
                callback(execResult);
            } catch (Exception e) {
                log.error("[task-executor] error [{}] jobNo:[{}] details[{}]", executorId, subTaskDO.getJobNo(), e);
            }
        }

        private void callback(ExecResult<?> execResult) {
            String jobNo = subTaskDO.getJobNo();
            Long batchJobId = subTaskDO.getBatchTaskId();
            log.info("[task-executor] [{}] exec task end jobNo [{}] exec-status[{}] ", executorId, jobNo, execResult.status.getDesc());
            if (null != execResult.getThrowable()) {
                subTaskDO.setFailureCnt(subTaskDO.getFailureCnt() + 1);
            }
            subTaskDO.setResult(JSONUtil.toJsonStr(execResult.getData()));
            subTaskDO.setExecStatus(subTaskDO.getFailureCnt() >= maxFailureCnt ? ExecStatus.ERROR : execResult.status);
            // 任务锁释放
            releaseLock();
            log.info("[task-executor] [{}]-[{}] lock release", executorId, subTaskDO.getJobNo());
            // 批任务尝试成功，根据已完成子任务列表
            if (execResult.status == ExecStatus.SUCCESS) {
                final int affectedRows = batchTaskMapper.updateSuccessWithCompletedSubTask(tablePrefix, batchJobId);
                if (affectedRows > 0) {
                    log.info("[task-executor] [{}]-[{}]-[{}] batch-task successful , change exec status ", executorId, jobNo, batchJobId);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private <T> ExecResult<T> execute() {
            final Stopwatch stopwatch = Stopwatch.createStarted();
            // TODO 检查上下游依赖
            checkDependOns();
            // 获取任务
            final BatchTaskHandler<?> handler = taskHandlerFactory.getNonNullHandler(subTaskDO.getTaskAddress());
            log.info("[task-executor] [{}] exec task jobNo [{}]-[{}] ", taskGroupConfig.getExecutorId(), subTaskDO.getJobNo(), subTaskDO.getTaskAddress());
            // 执行任务
            final T result = (T) handler.execute(subTaskDO.getParam());
            // 日志打印、结果集返回
            log.info("[task-executor] [{}] exec task end jobNo [{}]-[{}]-[{}]", taskGroupConfig.getExecutorId(),
                    subTaskDO.getJobNo(), subTaskDO.getTaskAddress(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
            return new ExecResult<>(ExecStatus.SUCCESS, result);
        }

        private void releaseLock() {
            batchSubTaskMapper.releaseLock(machineId.getId(), taskGroupConfig.getExecutorId(), subTaskDO);
        }

        private boolean acquireLock() {
            return batchSubTaskMapper.lockSubTask(machineId.getId(), taskGroupConfig.getExecutorId(), subTaskDO.getId()) > 0;
        }

        private void checkDependOns() {

        }
    }

    @Data
    @AllArgsConstructor
    static class ExecResult<T> {
        private ExecStatus status;
        private Throwable throwable;
        private T data;

        public ExecResult(ExecStatus status, T data) {
            this.status = status;
            this.data = data;
        }

        public static ExecResult<?> error(ExecStatus execStatus, Throwable throwable) {
            return new ExecResult<>(execStatus, throwable, null);
        }
    }


}
