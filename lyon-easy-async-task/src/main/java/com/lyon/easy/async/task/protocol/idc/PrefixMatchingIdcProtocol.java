package com.lyon.easy.async.task.protocol.idc;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.lyon.easy.async.task.util.ParamsCheckerUtil;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

/**
 * @author Lyon
 */
public class PrefixMatchingIdcProtocol extends AbstractIdcProtocol {

    @Override
    public void init() {
        ParamsCheckerUtil.check(idcProperties);
        final Map<String, String> matchIpList = idcProperties.getMatchIpList();
        matchIpList.forEach((idc, ipPrefixStr) -> {
            final InetAddress address = NetUtil.getLocalhost();
            idcList.add(idc);
            if (StrUtil.startWith(address.getHostAddress(), ipPrefixStr)) {
                this.idc = idc;
            }
        });
    }

    @Override
    public String getIdc() {
        return idc;
    }

    @Override
    public List<String> getIdcList() {
        return idcList;
    }

}
