package com.lyon.easy.async.task.util;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.lyon.easy.async.task.dal.dataobject.task.SubTaskDO;
import com.lyon.easy.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lyon
 */
@SuppressWarnings("unused")
@Slf4j
public class RuntimeTaskUtil {

    private static final Set<String> JOB_SETS = new ConcurrentHashSet<>();

    private static final Set<Long> TASK_SETS = new ConcurrentHashSet<>();

    private static final Map<String, Thread> JOB_THREAD_SETS = new ConcurrentHashMap<>();

    /**
     * Map<batchTaskId,List<jobNo/>/>
     */
    private static final ArrayListMultimap<Long, String> TASK_MULTIMAP = ArrayListMultimap.create();

    public static void registerJob(SubTaskDO subTaskDO) {
        // 注册任务
        JOB_SETS.add(subTaskDO.getJobNo());
        // 注册批任务
        TASK_SETS.add(subTaskDO.getBatchTaskId());
        // 注册任务线程
        JOB_THREAD_SETS.put(subTaskDO.getJobNo(), Thread.currentThread());
        synchronized (TASK_MULTIMAP) {
            TASK_MULTIMAP.put(subTaskDO.getBatchTaskId(), subTaskDO.getJobNo());
        }
    }

    public static void unregisterJob(SubTaskDO subTaskDO) {
        log.info("[runtime-task-util] unregisterJob start [{}]-[{}]", subTaskDO.getJobNo(), subTaskDO.getBatchTaskId());
        // 删除任务
        JOB_SETS.remove(subTaskDO.getJobNo());
        // 删除批任务
        synchronized (TASK_MULTIMAP) {
            TASK_MULTIMAP.remove(subTaskDO.getBatchTaskId(), subTaskDO.getJobNo());
            if (CollUtils.isEmpty(TASK_MULTIMAP.get(subTaskDO.getBatchTaskId()))) {
                TASK_SETS.remove(subTaskDO.getBatchTaskId());
            }
        }
        log.info("[runtime-task-util] unregisterJob successful [{}]-[{}]", subTaskDO.getJobNo(), subTaskDO.getBatchTaskId());
    }

    public static Set<String> getJobNos() {
        return JOB_SETS;
    }

    public static Set<Long> getTaskSets() {
        return TASK_SETS;
    }

    public static List<String> getJobNos(Long batchTaskId) {
        return Lists.newArrayList(TASK_MULTIMAP.get(batchTaskId));
    }

    public synchronized static void interruptedTask(Long batchTaskId) {
        // synchronized (TASK_MULTIMAP)：要么已经注销，要么就还是当前线程在跑，可以中断当前任务
        synchronized (TASK_MULTIMAP) {
            final List<String> jobNos = TASK_MULTIMAP.get(batchTaskId);
            if (jobNos.isEmpty()) {
                return;
            }
            for (String jobNo : jobNos) {
                // 线程中断
                final Thread thread = JOB_THREAD_SETS.get(jobNo);
                if (null != thread) {
                    thread.interrupt();
                }
            }

        }

    }

}
