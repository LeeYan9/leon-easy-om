package com.lyon.easy.async.task.core;

import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.lyon.easy.async.task.config.ExecutorConfig;
import com.lyon.easy.async.task.config.TaskGroupConfig;
import com.lyon.easy.async.task.converter.BatchTaskConverter;
import com.lyon.easy.async.task.converter.SubTaskConverter;
import com.lyon.easy.async.task.core.idc.IdcContainer;
import com.lyon.easy.async.task.dal.dataobject.task.BatchTaskDO;
import com.lyon.easy.async.task.dal.dataobject.task.SubTaskDO;
import com.lyon.easy.async.task.dal.mysql.task.BatchTaskMapper;
import com.lyon.easy.async.task.dal.mysql.task.BatchSubTaskMapper;
import com.lyon.easy.async.task.data.BatchTask;
import com.lyon.easy.async.task.data.SubTask;
import com.lyon.easy.async.task.enums.ExecStatus;
import com.lyon.easy.async.task.enums.IdcEnum;
import com.lyon.easy.async.task.factory.TaskHandlerFactory;
import com.lyon.easy.async.task.handler.TaskHandler;
import com.lyon.easy.async.task.util.ParamsCheckerUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.lyon.easy.common.utils.CollUtils.toNonEmptyList;

/**
 * @author Lyon
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
@Slf4j
public class BatchTaskManager implements ApplicationContextAware, SmartInitializingSingleton {

    @Getter
    private final ExecutorConfig executorConfig;

    private final String tablePrefix;

    private final MachineId machineId;

    private ExecutorManager executorManager;

    private final TaskHandlerFactory taskHandlerFactory;

    private final IdcContainer idcContainer;

    private AcquireTaskService acquireTaskService;

    private BatchTaskHeartBeatReactor heartBeatReactor;

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
        this.tablePrefix = "";
    }

    public void init() {
        ParamsCheckerUtil.check(this);
        this.executorManager = new ExecutorManager(executorConfig);
        this.acquireTaskService = new AcquireTaskService(this);
        this.heartBeatReactor = new BatchTaskHeartBeatReactor(this);
    }

    public List<SubTaskDO> determineTasksOfExec(TaskGroupConfig taskGroupConfig) {
        // FIXME multi idc ,  how to solve??
        //en-: if multiple idc , then multiple idc all right get that subtasks . need optimize
        //cn-: 如果是多个idc，那么多个idc都可以得到子任务。需要优化
        return batchSubTaskMapper.chooseSubTask(tablePrefix, idcContainer.idc(), taskGroupConfig.getGroups(), taskGroupConfig.getTaskLimit());
    }

    public void submitTask(BatchTask batchTask) {
        // write db
        ParamsCheckerUtil.check(batchTask);
        // save batch task
        final BatchTaskDO batchTaskDO = BatchTaskConverter.INSTANCE.from(batchTask);
        batchTaskMapper.insert(batchTaskDO);
        // save sub task list
        final List<SubTaskDO> listAll = Lists.newArrayList();
        batchTask.getSubTasks().forEach(subTask -> listAll.addAll(generateSubTaskDOList(batchTaskDO, subTask)));
        batchSubTaskMapper.insertBatch(listAll);
    }

    private List<SubTaskDO> generateSubTaskDOList(BatchTaskDO batchTaskDO, SubTask subTask) {
        List<SubTaskDO> subTaskDOList;
        if (IdcEnum.ALL == batchTaskDO.getIdcType()) {
            subTaskDOList = toNonEmptyList(idcContainer.idcList(), idc -> SubTaskConverter.INSTANCE.from(batchTaskDO, subTask, idc));
        } else {
            subTaskDOList = List.of(SubTaskConverter.INSTANCE.from(batchTaskDO, subTask));
        }
        return subTaskDOList;
    }

    public void kernelExecTask(TaskGroupConfig taskGroupConfig, SubTaskDO subTaskDO) {
        final BatchSubTaskRunnable worker = new BatchSubTaskRunnable(taskGroupConfig, subTaskDO);
        executorManager.getExecutor(taskGroupConfig.getName()).submit(worker);
    }

    public void releaseLockWhenHeartBeatExpireIn() {
        batchSubTaskMapper.releaseLockWhenHeartBeatExpireIn(tablePrefix);
    }

    public void renewHeartbeat() {
        final LocalDateTime nextExpireInTime = LocalDateTime.now().plus(executorConfig.getHearBeatTimeMills(), ChronoUnit.MILLIS);
        batchSubTaskMapper.renewHeartbeat(tablePrefix, machineId.getId(), nextExpireInTime);
    }

    @Override
    public void setApplicationContext(@SuppressWarnings("NullableProblems") ApplicationContext ac) throws BeansException {
        this.ac = ac;
    }

    @Override
    public void afterSingletonsInstantiated() {
//        this.taskMapper = ac.getBean(TaskMapper.class);
    }


    @AllArgsConstructor
    @Getter
    class BatchSubTaskRunnable implements Runnable {

        private final TaskGroupConfig taskGroupConfig;

        private final SubTaskDO subTaskDO;

        @Override
        public void run() {
            // real exec task
            // add table#sit_row && row-record add mutex lock
            ExecStatus execStatus = ExecStatus.FAILED;
            try {
                // 加锁
                if (!tryLockTask()) {
                    // TODO log xx
                    return;
                }
                // 检查上下游依赖 TODO
                // 确认子任务需求
                final String taskAddress = subTaskDO.getTaskAddress();
                // 执行子任务
                final TaskHandler handler = taskHandlerFactory.getNonNullHandler(taskAddress);
                handler.execute(subTaskDO.getParam());
                // 变更批任务、子任务 结果
                execStatus = ExecStatus.SUCCESS;
                releaseLock(execStatus);
            } catch (Exception e) {
                // add lock failed
                log.error("batchTaskExecutor error sub-task-id[{}] details[{}]", subTaskDO.getId(), JSONUtil.toJsonStr(subTaskDO), e);
                e.printStackTrace();
            } finally {
                // 解锁
                releaseLock(execStatus);
                log.info("[{}]-[{}]-[{}] execute lock release", machineId.getId(), taskGroupConfig.getExecutorId(), subTaskDO.getGroupName());
            }
            // 批任务尝试成功，根据已完成子任务列表
            batchTaskMapper.updateSuccessWithCompletedSubTask(tablePrefix, subTaskDO.getBatchTaskId());

        }

        private void releaseLock(ExecStatus execStatus) {
            batchSubTaskMapper.releaseLock(tablePrefix, machineId.getId(), taskGroupConfig.getExecutorId(), subTaskDO.getId(), execStatus);
        }

        private boolean tryLockTask() {
            // calculate lock expire in time
            final long heartBeatCheckerTime = executorConfig.getHearBeatTimeMills() * executorConfig.getHearBeatTimeoutInterval();
            LocalDateTime expireIn = LocalDateTime.now().plus(heartBeatCheckerTime, ChronoUnit.MILLIS);
            return batchSubTaskMapper.lockSubTask(tablePrefix, machineId.getId(), taskGroupConfig.getExecutorId(), subTaskDO.getId(), expireIn) > 0;
        }
    }

}
