package com.lyon.easy.async.task.dal.dataobject.task;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyon.easy.async.task.dal.dataobject.extend.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
/**
 * @author Lyon
 */
@TableName("batch_task_depend_on")
public class BatchTaskDependOnDO extends BaseDO {
}
