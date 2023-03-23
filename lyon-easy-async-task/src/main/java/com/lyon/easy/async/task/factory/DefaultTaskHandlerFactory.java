package com.lyon.easy.async.task.factory;

import com.lyon.easy.async.task.handler.BatchTaskHandler;
import com.lyon.easy.async.task.protocol.task.TaskHandlerProtocol;

import java.util.List;

/**
 * @author Lyon
 */
@SuppressWarnings("rawtypes")
public class DefaultTaskHandlerFactory implements TaskHandlerFactory {

    private final List<TaskHandlerProtocol> taskHandlerProtocols;

    public DefaultTaskHandlerFactory(List<TaskHandlerProtocol> taskHandlerProtocols) {
        this.taskHandlerProtocols = taskHandlerProtocols;
    }

    @Override
    public BatchTaskHandler getNonNullHandler(String taskAddress) {
        BatchTaskHandler batchTaskHandler = null;
        for (TaskHandlerProtocol taskHandlerProtocol : taskHandlerProtocols) {
            final boolean support = taskHandlerProtocol.support(taskAddress);
            if (support) {
                batchTaskHandler = taskHandlerProtocol.getHandler(taskAddress);
            }
        }
        return batchTaskHandler;
    }
}
