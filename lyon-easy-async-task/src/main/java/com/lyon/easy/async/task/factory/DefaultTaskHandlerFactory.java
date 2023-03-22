package com.lyon.easy.async.task.factory;

import com.lyon.easy.async.task.handler.TaskHandler;
import com.lyon.easy.async.task.protocol.task.TaskProtocol;

import java.util.List;

/**
 * @author Lyon
 */
public class DefaultTaskHandlerFactory implements TaskHandlerFactory {

    private final List<TaskProtocol> taskProtocols;

    public DefaultTaskHandlerFactory(List<TaskProtocol> taskProtocols) {
        this.taskProtocols = taskProtocols;
    }

    @Override
    public TaskHandler getNonNullHandler(String taskAddress) {
        TaskHandler taskHandler = null;
        for (TaskProtocol taskProtocol : taskProtocols) {
            final boolean support = taskProtocol.support(taskAddress);
            if (support) {
                taskHandler = taskProtocol.getHandler(taskAddress);
            }
        }
        return taskHandler;
    }
}
