package com.lyon.easy.async.task.core.idc;

import com.lyon.easy.async.task.protocol.idc.IdcProtocol;
import lombok.Data;

import java.util.List;

/**
 * @author Lyon
 */
@Data
public class IdcContainer {

    private final IdcProtocol idcProtocol;

    public String idc() {
        return idcProtocol.getIdc();
    }

    public List<String> idcList() {
        return idcProtocol.getIdcList();
    }


}
