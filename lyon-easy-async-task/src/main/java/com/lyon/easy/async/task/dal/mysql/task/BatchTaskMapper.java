package com.lyon.easy.async.task.dal.mysql.task;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyon.easy.async.task.config.mybatis.BaseXMapper;
import com.lyon.easy.async.task.dal.dataobject.task.BatchTaskDO;
import com.lyon.easy.async.task.enums.ExecStatus;
import com.lyon.easy.common.utils.CollUtils;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lyon
 */
@Mapper
public interface BatchTaskMapper extends BaseXMapper<BatchTaskDO> {

    /**
     * 更新批任务执行成功，根据已完成子任务列表
     *
     * @param batchTaskId 批任务ID
     * @return 影响行数
     */
    default int updateSuccessWithCompletedSubTask(Long batchTaskId) {
        final LambdaUpdateWrapper<BatchTaskDO> updateWrapper = Wrappers.lambdaUpdate(BatchTaskDO.class);
        final BatchTaskDO updateDO = new BatchTaskDO();
        updateDO.setExecStatus(ExecStatus.SUCCESS);
        updateWrapper
                .eq(BatchTaskDO::getId, batchTaskId)
                .ne(BatchTaskDO::getExecStatus, ExecStatus.SUCCESS.getCode())
                .notExists(
                        "(select * from sub_task " +
                                "        where batch_task_id = " + batchTaskId +
                                "        and exec_status <>1 )");
        return update(updateDO, updateWrapper);
    }

    /**
     * 根据批次号查询批次任务详情
     *
     * @param batchNo 批次号
     * @return 详情
     */
    default BatchTaskDO selectByBatchNo(String batchNo) {
        final LambdaQueryWrapper<BatchTaskDO> lambdaQuery = Wrappers.lambdaQuery(BatchTaskDO.class);
        lambdaQuery.eq(BatchTaskDO::getBatchNo, batchNo);
        return selectOne(lambdaQuery);
    }


    /**
     * 根据子任务状态变更批任务状态
     *
     * @param batchTaskId  batchTaskId
     * @param execStatuses 子任务预期状态
     * @param execStatus   更新状态
     * @return 影响行数
     */
    default int updateStatusWithSubTaskExpectStatuses(Long batchTaskId,
                                                      ExecStatus execStatus,
                                                      ArrayList<ExecStatus> execStatuses) {
        final LambdaUpdateWrapper<BatchTaskDO> updateWrapper = Wrappers.lambdaUpdate(BatchTaskDO.class);
        final BatchTaskDO updateDO = new BatchTaskDO();
        final String codes = StrUtil.join(",", CollUtils.toList(execStatuses, ExecStatus::getCode));

        updateDO.setExecStatus(execStatus);
        updateWrapper
                .set(BatchTaskDO::getNextStatus,null)
                .eq(BatchTaskDO::getId, batchTaskId)
                .notExists(
                        String.format("(select * from sub_task " +
                                "        where batch_task_id = " + batchTaskId +
                                "        and exec_status not in (%s) )", codes));
        return update(updateDO, updateWrapper);
    }

    /**
     * 根据当前批任务状态类型 变更 下个状态
     * @param batchTaskId 批任务id
     * @param nextStatus 变更状态
     * @param expectStatuses 预期状态列表
     * @return 影响行数
     */
    default int updateNextStatusWithStatuses(Long batchTaskId, ExecStatus nextStatus, List<ExecStatus> expectStatuses){
        final LambdaUpdateWrapper<BatchTaskDO> updateWrapper = Wrappers.lambdaUpdate(BatchTaskDO.class);
        final BatchTaskDO updateDO = new BatchTaskDO();
        updateDO.setNextStatus(nextStatus);
        updateWrapper
                .eq(BatchTaskDO::getId, batchTaskId)
                .in(BatchTaskDO::getExecStatus,expectStatuses);
        return update(updateDO, updateWrapper);
    }

    /**
     * 查询批任务列表
     * @param execStatus 执行状态
     * @return 结果集
     */
    default List<BatchTaskDO> selectListByNextStatus(ExecStatus execStatus) {
        final LambdaQueryWrapper<BatchTaskDO> lambdaQuery = Wrappers.lambdaQuery(BatchTaskDO.class);
        lambdaQuery.eq(BatchTaskDO::getNextStatus, execStatus);
        return selectList(lambdaQuery);
    }

    /**
     * 变更批任务执行状态，确认批任务最终状态。 nextStatus = null
     * @param batchTaskId 批任务id
     * @param execStatus 执行状态
     * @param expectStatuses 预期状态
     * @return 影响行数
     */
    default int updateStatusWithStatuses(Long batchTaskId, ExecStatus execStatus, List<ExecStatus> expectStatuses){
        final LambdaUpdateWrapper<BatchTaskDO> updateWrapper = Wrappers.lambdaUpdate(BatchTaskDO.class);
        final BatchTaskDO updateDO = new BatchTaskDO();
        updateDO.setExecStatus(execStatus);
        updateWrapper
                .set(BatchTaskDO::getNextStatus,null)
                .eq(BatchTaskDO::getId, batchTaskId)
                .in(BatchTaskDO::getExecStatus,expectStatuses);
        return update(updateDO, updateWrapper);
    }
}
