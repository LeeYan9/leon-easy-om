package com.lyon.easy.test.async.task.util;

import cn.hutool.core.util.RandomUtil;
import com.lyon.easy.async.task.data.BatchTask;
import com.lyon.easy.async.task.data.SubTask;
import com.lyon.easy.async.task.enums.IdcEnum;
import com.lyon.easy.async.task.protocol.task.TaskProtocols;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lyon
 */
public class GenerateUtil {

    @SuppressWarnings("SameParameterValue")
    public static <T> BatchTask generateExampleBatchTask(String groupName, IdcEnum idcEnum, int subCount, T target) {
        final BatchTask batchTask = new BatchTask();
        batchTask.setBatchNo(RandomUtil.randomStringUpper(5));
        batchTask.setDisplayName(RandomUtil.randomStringUpper(5));
        batchTask.setJobName(RandomUtil.randomStringUpper(5));
        batchTask.setGroupName(groupName);
        batchTask.setBatchNo(RandomUtil.randomStringUpper(5));
        batchTask.setIdcType(idcEnum);
        batchTask.setSrc("test-app");
        batchTask.setSubTasks(generateSubTasks(subCount, batchTask, target));
        return batchTask;
    }

    public static <T> List<SubTask> generateSubTasks(int count, BatchTask batchTask, T target) {
        if (IdcEnum.ANY == batchTask.getIdcType()) {
            count = 1;
        }
        return Stream.iterate(0, cnt -> cnt + 1).limit(count)
                .map(cnt -> {
                    final SubTask subTask = new SubTask();
                    subTask.setGroupName(batchTask.getGroupName());
//                    subTask.setTaskAddress(TaskProtocols.getTaskAddress(TaskProtocols.PROTOCOL, "simpleTaskHandler"));
                    subTask.setTaskAddress(TaskProtocols.getTaskAddress(TaskProtocols.BEAN, target));
                    subTask.setJobNo(RandomUtil.randomStringUpper(5));
                    subTask.setParam(String.format("{\"slice\":%s}", cnt));
                    return subTask;
                }).collect(Collectors.toList());
    }
}
