package com.sm.charge.cluster;

import java.util.UUID;

/**
 * @author shifeng.luo
 * @version created on 2017/11/3 下午2:40
 */
public interface ClusterSetting {

    String serverId = "cluster.com.sm.charge.raft.server.id";
    String defaultServerId = UUID.randomUUID().toString();

    String port = "cluster.server.port";
    int defaultPort = 55555;

    String transportType = "cluster.server.transport.type";
    String defaultTransportType = "netty";

    String serverType = "cluster.server.type";
    String defaultServerType = "data";

    String electTimeout = "cluster.server.group.elect.timeout";
    int defaultElectTimeout = 10000;

    String maxAppendSize = "cluster.server.append.size.max";
    int defaultMaxAppendSize = 5 * 1024 * 1024;
}
