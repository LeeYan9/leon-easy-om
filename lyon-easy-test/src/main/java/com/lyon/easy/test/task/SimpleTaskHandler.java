package com.lyon.easy.test.task;

import com.lyon.easy.async.task.handler.BatchTaskHandler;
import org.springframework.stereotype.Component;

/**
 * @author Lyon
 */
@Component("simpleTaskHandler")
public class SimpleTaskHandler implements BatchTaskHandler<String> {

    @Override
    public String execute(String param) {
        System.out.println(param);
        return "{\"execCode\":200}";
    }

}
