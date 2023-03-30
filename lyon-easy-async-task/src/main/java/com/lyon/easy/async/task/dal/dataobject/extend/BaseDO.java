package com.lyon.easy.async.task.dal.dataobject.extend;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

/**
 * del_flag,
 *         create_user,
 *         create_time,
 *         update_user,
 *         update_time,
 *         id, user_name
 * @author Lyon
 */
@Data
public abstract class BaseDO {

    private String creator;

    private Date createDate;

    private String updater;

    private Date updateDate;

    @TableLogic
    private Boolean deleted;



}
