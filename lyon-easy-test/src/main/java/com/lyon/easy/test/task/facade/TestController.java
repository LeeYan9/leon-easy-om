package com.lyon.easy.test.task.facade;

import cn.hutool.core.util.RandomUtil;
import com.lyon.easy.async.task.BatchTaskTemplate;
import com.lyon.easy.async.task.core.BatchTaskManager;
import com.lyon.easy.async.task.data.BatchTask;
import com.lyon.easy.async.task.data.SubTask;
import com.lyon.easy.async.task.enums.IdcEnum;
import com.lyon.easy.async.task.protocol.task.TaskProtocols;
import com.lyon.easy.common.base.R;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lyon
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private BatchTaskTemplate batchTaskTemplate;
    @Resource
    private BatchTaskManager batchTaskManager;

    @GetMapping("/isAlive")
    public R<String> isAlive() {
        return R.success("OK");
    }

    @GetMapping("/addBatchTask")
    public R<String> addBatchTask(@RequestParam(value = "enableIdcAll", required = false) Boolean enableIdcAll) {
        IdcEnum idcEnum = Objects.isNull(enableIdcAll) ? IdcEnum.ANY : IdcEnum.ALL;
        final BatchTask batchTask = generateExampleBatchTask("order-stat", idcEnum, 2, "simpleTaskHandler");
        batchTaskTemplate.submitTask(batchTask);
        return R.success("OK");
    }

    @GetMapping("/atOnce")
    public R<String> atOnce(@RequestParam(value = "group", required = false) String group) {
        batchTaskManager.atOnceExecute(group);
        return R.success("OK");
    }

    @GetMapping("/addLongBatchTask")
    public R<String> addLongBatchTask(@RequestParam(value = "enableIdcAll", required = false) Boolean enableIdcAll) {
        IdcEnum idcEnum = Objects.isNull(enableIdcAll) ? IdcEnum.ANY : IdcEnum.ALL;
        final BatchTask batchTask = generateExampleBatchTask("order-stat", idcEnum, 2,"simpleLongExecTaskHandler");
        batchTaskTemplate.submitTask(batchTask);
        return R.success("OK");
    }

    @SuppressWarnings("SameParameterValue")
    private static BatchTask generateExampleBatchTask(String groupName, IdcEnum idcEnum, int subCount, String beanName) {
        final BatchTask batchTask = new BatchTask();
        batchTask.setBatchNo(RandomUtil.randomStringUpper(5));
        batchTask.setDisplayName(RandomUtil.randomStringUpper(5));
        batchTask.setJobName(RandomUtil.randomStringUpper(5));
        batchTask.setGroupName(groupName);
        batchTask.setBatchNo(RandomUtil.randomStringUpper(5));
        batchTask.setIdcType(idcEnum);
        batchTask.setSrc("test-app");
        batchTask.setSubTasks(generateSubTasks(subCount, batchTask,beanName));
        return batchTask;
    }

    private static List<SubTask> generateSubTasks(int count, BatchTask batchTask, String beanName) {
        if (IdcEnum.ANY == batchTask.getIdcType()) {
            count = 1;
        }
        return Stream.iterate(0, cnt -> cnt + 1).limit(count)
                .map(cnt -> {
                    final SubTask subTask = new SubTask();
                    subTask.setGroupName(batchTask.getGroupName());
//                    subTask.setTaskAddress(TaskProtocols.getTaskAddress(TaskProtocols.PROTOCOL, "simpleTaskHandler"));
                    subTask.setTaskAddress(TaskProtocols.getTaskAddress(TaskProtocols.PROTOCOL,beanName));
                    subTask.setJobNo(RandomUtil.randomStringUpper(5));
                    subTask.setParam(String.format("{\"slice\":%s}", cnt));
                    return subTask;
                }).collect(Collectors.toList());
    }
}
