package com.lyon.easy.async.task.protocol.idc;

import com.lyon.easy.async.task.config.idc.IdcProperties;
import lombok.Setter;

import java.util.List;

/**
 * @author Lyon
 */
public abstract class AbstractIdcProtocol implements IdcProtocol {

    @Setter
    protected IdcProperties idcProperties;

    protected String idc;

    protected List<String> idcList;

    @Override
    public String getIdc() {
        return null;
    }

}
