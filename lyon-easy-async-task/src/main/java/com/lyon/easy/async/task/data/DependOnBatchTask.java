package com.lyon.easy.async.task.data;

import com.lyon.easy.async.task.enums.DependOnEnum;
import lombok.Data;

/**
 * @author lyon
 */
@Data
public class DependOnBatchTask {

    /**
     * 任务编号
     */
    private String batchNo;

    /**
     * 依赖类型
     */
    private DependOnEnum dependType;
}