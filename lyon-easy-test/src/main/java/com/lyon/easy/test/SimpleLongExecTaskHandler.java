package com.lyon.easy.test;

import cn.hutool.core.thread.ThreadUtil;
import com.lyon.easy.async.task.handler.BatchTaskHandler;
import org.springframework.stereotype.Component;

/**
 * @author Lyon
 */
@Component("simpleLongExecTaskHandler")
public class SimpleLongExecTaskHandler implements BatchTaskHandler<String> {

    @Override
    public String execute(String param) {
        System.out.println(param);
        ThreadUtil.safeSleep(10000);
        return "{\"execCode\":200}";
    }

}
