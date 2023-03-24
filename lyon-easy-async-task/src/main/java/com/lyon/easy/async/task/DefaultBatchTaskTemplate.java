package com.lyon.easy.async.task;

import com.lyon.easy.async.task.converter.BatchTaskConverter;
import com.lyon.easy.async.task.converter.SubTaskConverter;
import com.lyon.easy.async.task.core.BatchTaskManager;
import com.lyon.easy.async.task.dal.dataobject.task.BatchTaskDO;
import com.lyon.easy.async.task.dal.dataobject.task.SubTaskDO;
import com.lyon.easy.async.task.dal.mysql.task.BatchSubTaskMapper;
import com.lyon.easy.async.task.dal.mysql.task.BatchTaskMapper;
import com.lyon.easy.async.task.data.*;
import com.lyon.easy.common.utils.CollUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 批任务处理模板类
 *
 * @author lyon
 */
@SuppressWarnings("unused")
public class DefaultBatchTaskTemplate implements BatchTaskTemplate {

    @Resource
    private BatchTaskManager batchTaskManager;

    @Resource
    private BatchTaskMapper batchTaskMapper;

    @Resource
    private BatchSubTaskMapper batchSubTaskMapper;

    @Override
    public void submitTask(BatchTask batchTask) {
        batchTaskManager.submitTask(batchTask);
    }

    @Override
    public BatchTaskState getBatchTaskState(String batchNo) {
        BatchTaskDO batchTaskDO = batchTaskMapper.selectByBatchNo(batchNo);
        if (Objects.isNull(batchTaskDO)) {
            return null;
        }
        final List<SubTaskDO> subTaskDOList = batchSubTaskMapper.selectListByBatchId(batchTaskDO.getId());
        return generateBatchTaskState(batchTaskDO, subTaskDOList);
    }

    @Override
    public SubTaskState getSubTaskState(String jobNo) {
        SubTaskDO subTaskDO = batchSubTaskMapper.selectByJobNo(jobNo);
        return generateSubTaskState(subTaskDO);
    }

    BatchTaskState generateBatchTaskState(BatchTaskDO batchTaskDO, List<SubTaskDO> subTaskDOList) {
        final BatchTaskState batchTaskState = new BatchTaskState();
        batchTaskState.setBatchTask(new TaskExecStateDesc<>());
        // 批任务详情
        final BatchTask batchTask = BatchTaskConverter.INSTANCE.to(batchTaskDO);
        batchTaskState.getBatchTask().setStatus(batchTaskDO.getExecStatus());
        batchTaskState.getBatchTask().setData(batchTask);
        // 子任务详情列表
        final List<SubTaskState> subTaskStates = CollUtils.toList(subTaskDOList, this::generateSubTaskState);
        batchTaskState.setSubTasks(subTaskStates);
        return batchTaskState;
    }

    SubTaskState generateSubTaskState(SubTaskDO subTaskDO) {
        final SubTaskState subTaskState = new SubTaskState();
        subTaskState.setSubTask(new TaskExecStateDesc<>());
        final SubTask subTask = SubTaskConverter.INSTANCE.to(subTaskDO);
        subTaskState.getSubTask().setStatus(subTaskDO.getExecStatus());
        subTaskState.getSubTask().setResultMsg(subTaskDO.getResult());
        subTaskState.getSubTask().setData(subTask);
        return subTaskState;
    }
}
