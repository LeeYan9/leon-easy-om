package com.lyon.easy.async.task.core;

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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
                        taskGroupConfig.setExecutorId(String.format("%s-%s-%s", machineId.getId(), taskGroupConfig.getName(), taskGroupConfig.getGroups())));
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
//        batchSubTaskMapper.insertBatchSomeColumn(listAll); 导致字段默认值失效
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

    public void releaseLockWhenHeartBeatExpireIn() {
        batchSubTaskMapper.releaseLockWhenHeartBeatExpireIn();
    }

    public void renewHeartbeat() {
        final LocalDateTime nextExpireInTime = LocalDateTime.now().plus(executorConfig.getHearBeatTimeMills(), ChronoUnit.MILLIS);
        batchSubTaskMapper.renewHeartbeat(machineId.getId(), nextExpireInTime);
    }

    @Override
    public void setApplicationContext(@SuppressWarnings("NullableProblems") ApplicationContext ac) throws BeansException {
        this.ac = ac;
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }


    @AllArgsConstructor
    @Getter
    class TaskWorkRunnable implements Runnable {

        private final TaskGroupConfig taskGroupConfig;

        private final SubTaskDO subTaskDO;

        @Override
        public void run() {
            // real exec task
            // add table#sit_row && row-record add mutex lock
            ExecStatus execStatus = ExecStatus.FAILED;
            String execResult = null;
            try {
                log.info("[task-executor] [{}] ready add lock and exec sub-task jobNo [{}] "
                        , taskGroupConfig.getExecutorId(), subTaskDO.getJobNo());
                // 加锁
                if (!tryLockTask()) {
                    log.info("[task-executor] [{}] add lock failed sub-task jobNo [{}] ", taskGroupConfig.getExecutorId(), subTaskDO.getJobNo());
                    return;
                }
                log.info("[task-executor] [{}] add lock successful sub-task jobNo [{}] "
                        , taskGroupConfig.getExecutorId(), subTaskDO.getJobNo());
                // 检查上下游依赖 TODO
                // 确认子任务需求
                final String taskAddress = subTaskDO.getTaskAddress();
                // 执行子任务
                final BatchTaskHandler<?> handler = taskHandlerFactory.getNonNullHandler(taskAddress);
                final Stopwatch stopwatch = Stopwatch.createStarted();
                log.info("[task-executor] [{}] ready exec sub-task jobNo [{}]-[{}] ", taskGroupConfig.getExecutorId(), subTaskDO.getJobNo(), taskAddress);
                final Object result = handler.execute(subTaskDO.getParam());
                execResult = JSONUtil.toJsonStr(result);
                // 变更批任务、子任务 结果
                execStatus = ExecStatus.SUCCESS;
                stopwatch.stop();
                log.info("[task-executor] [{}] exec sub-task end jobNo [{}]-[{}]-[{}]", taskGroupConfig.getExecutorId(),
                        subTaskDO.getJobNo(), taskAddress, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                releaseLock(execStatus,execResult);
            } catch (Exception e) {
                // add lock failed
                log.error("[task-executor] error sub-task-id[{}] details[{}]", subTaskDO.getId(), JSONUtil.toJsonStr(subTaskDO), e);
                e.printStackTrace();
            } finally {
                log.info("[task-executor] [{}] exec end sub-task start jobNo [{}] exec-status[{}] "
                        , taskGroupConfig.getExecutorId(), subTaskDO.getJobNo(), execStatus.getDesc());
                // 解锁
                releaseLock(execStatus, execResult);
                log.info("[task-executor] [{}]-[{}] lock release", taskGroupConfig.getExecutorId(), subTaskDO.getJobNo());
            }
            // 批任务尝试成功，根据已完成子任务列表
            final int affectedRows = batchTaskMapper.updateSuccessWithCompletedSubTask(tablePrefix, subTaskDO.getBatchTaskId());
            if (affectedRows > 0) {
                log.info("[task-executor] [{}]-[{}]-[{}] batch-task exec successful , change exec status ", taskGroupConfig.getExecutorId(), subTaskDO.getJobNo(), subTaskDO.getBatchTaskId());
            }
        }

        private void releaseLock(ExecStatus execStatus, String execResult) {
            batchSubTaskMapper.releaseLock(machineId.getId(), taskGroupConfig.getExecutorId(), subTaskDO.getId(), execStatus,execResult);
        }

        private boolean tryLockTask() {
            // calculate lock expireAt time
            final long heartBeatCheckerTime = executorConfig.getHearBeatTimeMills() * executorConfig.getHearBeatTimeoutInterval();
            LocalDateTime expireIn = LocalDateTime.now().plus(heartBeatCheckerTime, ChronoUnit.MILLIS);
            return batchSubTaskMapper.lockSubTask(machineId.getId(), taskGroupConfig.getExecutorId(), subTaskDO.getId(), expireIn) > 0;
        }
    }

}
