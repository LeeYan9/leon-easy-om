package com.lyon.easy.async.task.dal.mysql.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyon.easy.async.task.config.mybatis.BaseXMapper;
import com.lyon.easy.async.task.dal.dataobject.task.BatchTaskDO;
import com.lyon.easy.async.task.enums.ExecStatus;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Lyon
 */
@Mapper
public interface BatchTaskMapper extends BaseXMapper<BatchTaskDO> {

    /**
     * 更新批任务执行成功，根据已完成子任务列表
     *
     * @param tablePrefix 表前缀
     * @param batchTaskId 批任务ID
     * @return 影响行数
     */
    default int updateSuccessWithCompletedSubTask(String tablePrefix, Long batchTaskId) {
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
}
