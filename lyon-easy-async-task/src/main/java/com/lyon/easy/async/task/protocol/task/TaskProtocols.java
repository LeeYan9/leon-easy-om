package com.lyon.easy.async.task.protocol.task;

import cn.hutool.core.util.StrUtil;

/**
 * @author Lyon
 */
public interface TaskProtocols {

    String PROTOCOL = "BEAN://";

    /**
     * 获取任务地址
     *
     * @param protocol 协议类型
     * @param data     数据
     * @param <T>      泛型
     * @return 完整任务地址
     */
    static <T> String getTaskAddress(String protocol, T data) {
        if (StrUtil.equals(PROTOCOL, protocol)) {
            String canonicalName = data.getClass().getCanonicalName();
            if (data instanceof Class) {
                //noinspection rawtypes
                canonicalName = ((Class) data).getCanonicalName();
            } else if (data instanceof String) {
                canonicalName = (String) data;
            }
            return String.format("%s%s", protocol, canonicalName);
        }
        return null;
    }

}
