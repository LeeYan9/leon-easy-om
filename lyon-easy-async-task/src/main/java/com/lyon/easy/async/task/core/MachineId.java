package com.lyon.easy.async.task.core;

import cn.hutool.core.net.NetUtil;
import com.lyon.easy.async.task.core.idc.IdcContainer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetAddress;

/**
 * @author Lyon
 */
@RequiredArgsConstructor
public class MachineId implements InitializingBean {

    private final IdcContainer idcContainer;

    @Getter
    private String id;


    @Override
    public void afterPropertiesSet() {
        final InetAddress localhost = NetUtil.getLocalhost();
        final String hostAddress = localhost.getHostAddress();
        this.id = hostAddress + idcContainer.idc();
    }
}
