package com.lyon.easy.async.task.dal.mysql.task;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyon.easy.async.task.config.mybatis.BaseXMapper;
import com.lyon.easy.async.task.dal.dataobject.task.SubTaskDO;
import com.lyon.easy.async.task.enums.EnableEnum;
import com.lyon.easy.async.task.enums.ExecStatus;
import com.lyon.easy.async.task.enums.IdcEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lyon
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Mapper
public interface BatchSubTaskMapper extends BaseXMapper<SubTaskDO> {

    /**
     * 加锁
     *
     * @param clientId 机器id
     * @param owner    持有者
     * @param id       id
     * @return 影响行数
     */
    default int lockSubTask(String clientId,
                            String owner,
                            Long id) {
        final SubTaskDO updateEntity = new SubTaskDO();
        updateEntity.setOwner(owner);
        updateEntity.setClientId(clientId);
        updateEntity.setLockStatus(EnableEnum.YES.ordinal());
        updateEntity.setLastHeartbeatTime(LocalDateTime.now());
        final LambdaUpdateWrapper<SubTaskDO> updateWrapper = Wrappers.lambdaUpdate(SubTaskDO.class);
        updateWrapper
                .eq(SubTaskDO::getId, id)
                .eq(SubTaskDO::getLockStatus, EnableEnum.NO.ordinal())
                .in(SubTaskDO::getExecStatus, ExecStatus.LOCKABLE_STATUES);

        return update(updateEntity, updateWrapper);
    }

    /**
     * 锁释放
     *
     * @param clientId  机器id
     * @param owner     持有者
     * @param subTaskDO 任务
     * @return 影响行数
     */
    default int releaseLock(String clientId,
                            String owner,
                            SubTaskDO subTaskDO) {
        final LambdaUpdateWrapper<SubTaskDO> wrapper = Wrappers.lambdaUpdate(SubTaskDO.class);
        final String str = StrUtil.sub(subTaskDO.getResult(), 0, 1000);
        wrapper.eq(SubTaskDO::getId, subTaskDO.getId())
                .eq(SubTaskDO::getClientId, clientId)
                .eq(SubTaskDO::getOwner, owner)
                .set(SubTaskDO::getLastHeartbeatTime, null)
                .set(SubTaskDO::getResult, str);
        final SubTaskDO updateEntity = new SubTaskDO();
        updateEntity.setLockStatus(EnableEnum.NO.ordinal());
        updateEntity.setFailureCnt(subTaskDO.getFailureCnt());
        updateEntity.setExecStatus(subTaskDO.getExecStatus());
        return update(updateEntity, wrapper);
    }

    /**
     * 选择子任务列表
     *
     * @param idc       机房
     * @param groups    组名称列表
     * @param taskLimit 限制
     * @return 影响行数
     */
    default List<SubTaskDO> chooseSubTask(String idc,
                                          List<String> groups,
                                          int taskLimit) {
        final String groupNames = StrUtil.join(",", groups);
        final LambdaQueryWrapper<SubTaskDO> queryWrapper = Wrappers.lambdaQuery(SubTaskDO.class);
        //noinspection CodeBlock2Expr
        queryWrapper
                .eq(SubTaskDO::getLockStatus, EnableEnum.NO.ordinal())
                .in(SubTaskDO::getExecStatus, ExecStatus.LOCKABLE_STATUES)
                .and(wrapper -> {
                    wrapper
                            .or().eq(SubTaskDO::getIdc, idc)
                            .or().eq(SubTaskDO::getIdcType, IdcEnum.ANY.getCode());
                })
                .apply(" FIND_IN_SET(`group_name`,\"" + groupNames + "\") ")
                .last(" limit " + taskLimit);
        return selectList(queryWrapper);
    }

    /**
     * 释放锁，如果心跳过期
     *
     * @param minHeartbeatTime minHeartbeatTime
     * @return 影响行数
     */
    default int releaseLockWhenHeartBeatExpireIn(LocalDateTime minHeartbeatTime) {
        final SubTaskDO updateDO = new SubTaskDO();
        updateDO.setLockStatus(EnableEnum.NO.ordinal());
        final LambdaUpdateWrapper<SubTaskDO> updateWrapper = Wrappers.lambdaUpdate(SubTaskDO.class);
        updateWrapper
                .eq(SubTaskDO::getLockStatus, EnableEnum.YES.ordinal())
                .in(SubTaskDO::getExecStatus, ExecStatus.RELEASABLE_STATUES)
                .lt(SubTaskDO::getLastHeartbeatTime, minHeartbeatTime)
                .set(SubTaskDO::getLastHeartbeatTime, null)
                .set(SubTaskDO::getOwner, null)
                .set(SubTaskDO::getClientId, null);
        return update(updateDO, updateWrapper);
    }

    /**
     * 心跳续期
     *
     * @param machineId 机器id
     * @return 影响行数
     */
    default int renewHeartbeat(String machineId) {
        final SubTaskDO updateDO = new SubTaskDO();
        updateDO.setLastHeartbeatTime(LocalDateTime.now());
        final LambdaUpdateWrapper<SubTaskDO> updateWrapper = Wrappers.lambdaUpdate(SubTaskDO.class);
        updateWrapper
                .eq(SubTaskDO::getLockStatus, EnableEnum.YES.ordinal())
                .eq(SubTaskDO::getClientId, machineId);
        return update(updateDO, updateWrapper);
    }

    /**
     * 查询子任务
     *
     * @param jobNo 任务编号
     * @return 详情
     */
    default SubTaskDO selectByJobNo(String jobNo) {
        final LambdaQueryWrapper<SubTaskDO> lambdaQuery = Wrappers.lambdaQuery(SubTaskDO.class);
        lambdaQuery.eq(SubTaskDO::getJobNo, jobNo);
        return selectOne(lambdaQuery);
    }

    /**
     * 查询子任务列表
     *
     * @param batchId 任务ID
     * @return 详情
     */
    default List<SubTaskDO> selectListByBatchId(Long batchId) {
        final LambdaQueryWrapper<SubTaskDO> lambdaQuery = Wrappers.lambdaQuery(SubTaskDO.class);
        lambdaQuery.eq(SubTaskDO::getBatchTaskId, batchId);
        return selectList(lambdaQuery);
    }
}
