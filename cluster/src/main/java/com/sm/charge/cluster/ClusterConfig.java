package com.sm.charge.cluster;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.base.Configure;

/**
 * @author shifeng.luo
 * @version created on 2017/11/3 下午2:38
 */
public class ClusterConfig {

    private final Configure configure;

    public ClusterConfig(Configure configure) {
        this.configure = configure;
    }

    public String getServerId(Address address) {
        return configure.getString(ClusterSetting.serverId, address.getAddressStr());
    }

    public ServerType getServerType() {
        String type = configure.getString(ClusterSetting.serverType, ClusterSetting.defaultServerType);
        return ServerType.valueOf(type);
    }

    public int getBindPort() {
        return configure.getInt(ClusterSetting.port, ClusterSetting.defaultPort);
    }

    public int getElectTimeout() {
        return configure.getInt(ClusterSetting.electTimeout, ClusterSetting.defaultElectTimeout);
    }

    public int getMaxAppendSize() {
        return configure.getInt(ClusterSetting.maxAppendSize, ClusterSetting.defaultMaxAppendSize);
    }
}
