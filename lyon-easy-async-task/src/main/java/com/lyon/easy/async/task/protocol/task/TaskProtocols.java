package com.lyon.easy.async.task.protocol.task;

import cn.hutool.core.util.StrUtil;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;

/**
 * @author Lyon
 */
public interface TaskProtocols {

    String BEAN = "BEAN://";

    /**
     * 获取任务地址
     *
     * @param protocol 协议类型
     * @param data     数据
     * @param <T>      泛型
     * @return 完整任务地址
     */
    static <T> String getTaskAddress(String protocol, T data) {
        if (StrUtil.equals(BEAN, protocol)) {
            String beanName;
            if (data instanceof Class) {
                beanName = Introspector.decapitalize(ClassUtils.getShortName(((Class<?>) data).getName()));
            } else if (data instanceof String) {
                beanName = (String) data;
            } else {
                beanName = Introspector.decapitalize(ClassUtils.getShortName(data.getClass().getName()));
            }
            return String.format("%s%s", protocol, beanName);
        }
        return null;
    }

}
