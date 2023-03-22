package com.lyon.easy.async.task.protocol.idc;

import java.util.List;

/**
 * @author Lyon
 */
public interface IdcProtocol {

    /**
     * 解析当前机房标识
     * @return idc
     */
    String getIdc();


    /**
     * 获取机房标识
     * @return idcList
     */
    List<String> getIdcList();

    /**
     * init
     */
    void init();

}
