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
import com.lyon.easy.async.task.util.RuntimeTaskUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.lyon.easy.common.utils.CollUtils.toNonEmptyList;

/**
 * @author Lyon
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
@Slf4j
public class BatchTaskManager implements ApplicationContextAware, InitializingBean, DisposableBean {

    @Getter
    private final ExecutorConfig executorConfig;

    private final String tablePrefix;

    private final MachineId machineId;

    private final ExecutorManager executorManager;

    private final TaskHandlerFactory taskHandlerFactory;

    private final IdcContainer idcContainer;

    private final AcquireTaskService acquireTaskService;

    private final BatchTaskHeartBeatReactor heartBeatReactor;

    private final BatchTaskCancelReactor taskCancelReactor;

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
        this.taskCancelReactor = new BatchTaskCancelReactor(this);
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
        this.taskCancelReactor.init();
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
        long heartBeatCheckerTime = executorConfig.getHeartBeatIntervalMs() * executorConfig.getHearBeatTimeoutInterval();
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

    @Override
    public void destroy() throws Exception {
        // TODO close()
    }

    public boolean interruptTask(Long batchTaskId) {
        List<ExecStatus> expectStatuses = Lists.newArrayList(ExecStatus.FAILED, ExecStatus.PAIR, ExecStatus.RUNNING,
                ExecStatus.INIT);
        return batchTaskMapper.updateNextStatusWithStatuses(batchTaskId, ExecStatus.INTERRUPT, expectStatuses) > 0;
    }

    public boolean cancelTask(Long batchTaskId) {
        List<ExecStatus> expectStatuses = Lists.newArrayList(ExecStatus.FAILED, ExecStatus.PAIR, ExecStatus.RUNNING,
                ExecStatus.INIT);
        return batchTaskMapper.updateNextStatusWithStatuses(batchTaskId, ExecStatus.CANCEL, expectStatuses) > 0;
    }

    public void doCancelTask() {
        List<BatchTaskDO> cancelTasks = batchTaskMapper.selectListByNextStatus(ExecStatus.CANCEL);
        for (BatchTaskDO cancelTask : cancelTasks) {
            // 抢占批任务取消的资格
            List<ExecStatus> expectStatuses = Lists.newArrayList(ExecStatus.FAILED, ExecStatus.PAIR, ExecStatus.RUNNING,
                    ExecStatus.INIT);
            int affectedRows = batchTaskMapper.updateStatusWithStatuses(cancelTask.getId(), ExecStatus.CANCEL, expectStatuses);
            if (affectedRows >= 0) {
                log.info("[cancel-task] batch task cancel successful [{}]-[{}]", cancelTask.getId(), cancelTask.getBatchNo());
                expectStatuses = Lists.newArrayList(ExecStatus.FAILED, ExecStatus.INIT);
                affectedRows = batchSubTaskMapper.updateStatusWithTaskIdAndStatuses(cancelTask.getId(), ExecStatus.CANCEL, expectStatuses);
                log.info("[cancel-task] batch sub task cancel successful [{}]-[{}] affectedRows:{}", cancelTask.getId(), cancelTask.getBatchNo(), affectedRows);
            }
        }
    }

    public void doInterruptTask() {
        List<BatchTaskDO> interruptTasks = batchTaskMapper.selectListByNextStatus(ExecStatus.INTERRUPT);
        for (BatchTaskDO interruptTask : interruptTasks) {
            RuntimeTaskUtil.interruptedTask(interruptTask.getId());
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
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
            try {
                run0();
            } catch (InterruptedException e) {
                // 清空中断位
                Thread.interrupted();
                log.error("[task-executor] interrupted successful [{}] [{}]", executorId, subTaskDO.getJobNo());
                callback(new ExecResult<>(ExecStatus.INTERRUPT, null));
            }
        }

        private void run0() throws InterruptedException {
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
                RuntimeTaskUtil.registerJob(subTaskDO);
                // 调用
                log.info("[task-executor] [{}] add lock successful jobNo [{}] ", executorId, subTaskDO.getJobNo());
                execResult = execute();
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                execResult = ExecResult.error(ExecStatus.FAILED, e);
                log.error("[task-executor] error [{}] details[{}]", executorId, JSONUtil.toJsonStr(subTaskDO), e);
            }
            try {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                // FIXME  可以尝试注册结果回调，链式执行
                // You can try registering the result callback, chaining execution
                callback(execResult);
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                log.error("[task-executor] error [{}] jobNo:[{}] details[{}]", executorId, subTaskDO.getJobNo(), e);
            }
        }

        private void callback(ExecResult<?> execResult) {
            String jobNo = subTaskDO.getJobNo();
            Long batchJobId = subTaskDO.getBatchTaskId();
            log.info("[task-executor] [{}] exec task end jobNo [{}] exec-status[{}] ", executorId, jobNo, execResult.status);
            if (null != execResult.getThrowable()) {
                subTaskDO.setFailureCnt(subTaskDO.getFailureCnt() + 1);
            }
            subTaskDO.setResult(JSONUtil.toJsonStr(execResult.getData()));
            subTaskDO.setExecStatus(subTaskDO.getFailureCnt() >= maxFailureCnt ? ExecStatus.ERROR : execResult.status);
            // 任务锁释放：<CANCEL,INTERRUPTED,SUCCESS,FAILED,ERROR>
            int affectedRows = releaseLock();
            if (affectedRows <= 0) {
                // ps: job 执行完成后，触发中断的情况， affectedRows=0
                log.info("[task-executor] [{}]-[{}] release [{}] failed affectedRows is empty", executorId, execResult.status, subTaskDO.getJobNo());
                RuntimeTaskUtil.unregisterJob(subTaskDO);
            } else {
                log.info("[task-executor] [{}]-[{}] release [{}] successful ", executorId, execResult.status, subTaskDO.getJobNo());
                RuntimeTaskUtil.unregisterJob(subTaskDO);
                // 批任务尝试成功，根据已完成子任务列表
                if (execResult.status == ExecStatus.SUCCESS) {
                    affectedRows = batchTaskMapper.updateSuccessWithCompletedSubTask(batchJobId);
                    if (affectedRows > 0) {
                        log.info("[task-executor] [{}]-[{}]-[{}] batch-task successful , change exec status ", executorId, jobNo, batchJobId);
                    }
                } else if (execResult.status == ExecStatus.INTERRUPT) {
                    // 批任务尝试中断完成，（所有子任务是 中断，成功，异常状态时）-> 不可扭转的负向状态
                    final ArrayList<ExecStatus> expectStatuses = Lists.newArrayList(ExecStatus.INTERRUPT, ExecStatus.SUCCESS, ExecStatus.ERROR);
                    affectedRows = batchTaskMapper.updateStatusWithSubTaskExpectStatuses(batchJobId, execResult.status, expectStatuses);
                    if (affectedRows > 0) {
                        log.info("[task-executor] [{}]-[{}]-[{}] batch-task interrupted successful , change exec status ", executorId, jobNo, batchJobId);
                    }
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

        private int releaseLock() {
            return batchSubTaskMapper.releaseLock(machineId.getId(), taskGroupConfig.getExecutorId(), subTaskDO);
        }

        private boolean acquireLock() throws InterruptedException {
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
