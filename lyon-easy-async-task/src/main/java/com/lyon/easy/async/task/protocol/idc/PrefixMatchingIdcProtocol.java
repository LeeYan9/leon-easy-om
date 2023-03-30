package com.lyon.easy.async.task.protocol.idc;

import cn.hutool.core.util.StrUtil;
import com.lyon.easy.async.task.util.ParamsCheckerUtil;
import com.lyon.easy.common.utils.HostUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Lyon
 */
public class PrefixMatchingIdcProtocol extends AbstractIdcProtocol {

    @Override
    public void init() {
        ParamsCheckerUtil.check(idcProperties);
        final Map<String, List<String>> matchIpList = idcProperties.getMatchIpList();
        matchIpList.forEach((idc, ipPrefixList) -> {
            final String address = HostUtils.getHostIp();
            idcList.add(idc);
            ipPrefixList.forEach(ipPrefixStr -> {
                if (StrUtil.startWith(address, ipPrefixStr)) {
                    this.idc = idc;
                }
            });
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
