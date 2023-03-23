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
import org.apache.ibatis.annotations.Param;

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
     * @param tablePrefix 表前缀
     * @param clientId    机器id
     * @param owner       持有者
     * @param id          id
     * @param expireAt    锁过期时间
     * @return 影响行数
     */
    default int lockSubTask(String tablePrefix,
                            String clientId,
                            String owner,
                            Long id,
                            LocalDateTime expireAt) {
        final SubTaskDO updateEntity = new SubTaskDO();
        updateEntity.setId(id);
        updateEntity.setOwner(owner);
        updateEntity.setClientId(clientId);
        updateEntity.setLockExpireAt(expireAt);
        return updateById(updateEntity);
    }

    /**
     * 锁释放
     *
     * @param tablePrefix 表前缀
     * @param clientId    机器id
     * @param owner       持有者
     * @param id          id
     * @param execStatus  执行状态
     * @return 影响行数
     */
    default int releaseLock(String tablePrefix,
                            String clientId,
                            String owner,
                            Long id,
                            ExecStatus execStatus) {
        final LambdaQueryWrapper<SubTaskDO> wrapper = Wrappers.lambdaQuery(SubTaskDO.class);
        wrapper.eq(SubTaskDO::getId, id)
                .eq(SubTaskDO::getClientId, clientId)
                .eq(SubTaskDO::getOwner, owner)
                .isNull(SubTaskDO::getOwner)
                .isNull(SubTaskDO::getClientId)
                .isNull(SubTaskDO::getLockExpireAt);
        final SubTaskDO updateEntity = new SubTaskDO();
        updateEntity.setExecStatus(execStatus);
        return update(updateEntity, wrapper);
    }

    /**
     * 选择子任务列表
     *
     * @param tablePrefix 表前缀
     * @param idc         机房
     * @param groups      组名称列表
     * @param taskLimit   限制
     * @return 影响行数
     */
    default List<SubTaskDO> chooseSubTask(@Param("tablePrefix") String tablePrefix,
                                          String idc,
                                          List<String> groups,
                                          int taskLimit) {
        final String groupNames = StrUtil.join(",", groups);
        final LambdaQueryWrapper<SubTaskDO> queryWrapper = Wrappers.lambdaQuery(SubTaskDO.class);
        //noinspection CodeBlock2Expr
        queryWrapper

                .or(wrapper -> {
                    wrapper
                            .or().eq(SubTaskDO::getIdc, idc)
                            .or().eq(SubTaskDO::getIdc, IdcEnum.ANY);
                })
                .apply(" FIND_IN_SET(\"" + groupNames + "\",`group_name`) ")
                .last(" limit " + taskLimit);
        return selectList(queryWrapper);
    }

    /**
     * 释放锁，如果心跳过期
     *
     * @param tablePrefix 表前缀
     * @return 影响行数
     */
    default int releaseLockWhenHeartBeatExpireIn(String tablePrefix) {
        final SubTaskDO updateDO = new SubTaskDO();
        updateDO.setLockStatus(EnableEnum.NO.ordinal());
        final LambdaUpdateWrapper<SubTaskDO> updateWrapper = Wrappers.lambdaUpdate(SubTaskDO.class);
        updateWrapper
                .apply(" lock_expire_in < now()")
                .isNotNull(SubTaskDO::getLockStatus)
                .isNull(SubTaskDO::getLockExpireAt)
                .isNull(SubTaskDO::getOwner)
                .isNull(SubTaskDO::getClientId);
        return update(updateDO, updateWrapper);
    }

    /**
     * 心跳续期
     *
     * @param tablePrefix 表前缀
     * @param machineId   机器id
     * @param expireAt    过期时间
     * @return 影响行数
     */
    default int renewHeartbeat(String tablePrefix,
                               String machineId,
                               LocalDateTime expireAt) {
        final SubTaskDO updateDO = new SubTaskDO();
        updateDO.setLockExpireAt(expireAt);
        updateDO.setLockStatus(EnableEnum.NO.ordinal());
        final LambdaUpdateWrapper<SubTaskDO> updateWrapper = Wrappers.lambdaUpdate(SubTaskDO.class);
        updateWrapper
                .eq(SubTaskDO::getLockStatus, EnableEnum.YES.ordinal())
                .eq(SubTaskDO::getClientId, machineId);
        return update(updateDO, updateWrapper);
    }

    /**
     * 查询子任务
     * @param jobNo 任务编号
     * @return 详情
     */
    default SubTaskDO selectByJobNo(String jobNo){
        final LambdaQueryWrapper<SubTaskDO> lambdaQuery = Wrappers.lambdaQuery(SubTaskDO.class);
        lambdaQuery.eq(SubTaskDO::getJobNo,jobNo);
        return selectOne(lambdaQuery);
    }

    /**
     * 查询子任务列表
     * @param batchId 任务ID
     * @return 详情
     */
    default List<SubTaskDO> selectListByBatchId(Long batchId){
        final LambdaQueryWrapper<SubTaskDO> lambdaQuery = Wrappers.lambdaQuery(SubTaskDO.class);
        lambdaQuery.eq(SubTaskDO::getBatchTaskId,batchId);
        return selectList(lambdaQuery);
    }
}
