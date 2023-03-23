package com.lyon.easy.async.task.core;

import cn.hutool.core.net.NetUtil;
import com.lyon.easy.async.task.core.idc.IdcContainer;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.InetAddress;

/**
 * @author Lyon
 */
@NoArgsConstructor
public class MachineId {

    private IdcContainer idcContainer;

    public MachineId(IdcContainer idcContainer) {
        this.idcContainer = idcContainer;
    }

    @Getter
    private String id;


    public void init() {
        final InetAddress localhost = NetUtil.getLocalhost();
        final String hostAddress = localhost.getHostAddress();
        this.id = String.format("[%s]-[%s]",hostAddress,idcContainer.idc());
    }
}
