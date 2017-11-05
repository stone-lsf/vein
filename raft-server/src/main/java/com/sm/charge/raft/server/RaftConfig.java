package com.sm.charge.raft.server;

import com.sm.finance.charge.common.base.Configure;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午10:52
 */
public class RaftConfig {

    private final Configure configure;

    /**
     * 复制日志超时时间(ms)
     */
    private static final String REPLICATE_TIMEOUT = "raft.server.replicate.timeout";
    private static final int DEFAULT_REPLICATE_TIMEOUT = 6000;

    /**
     * 集群名称
     */
    private static final String CLUSTER_NAME = "raft.cluster.name";
    private static final String DEFAULT_CLUSTER_NAME = "charge";

    private static final String PORT = "raft.server.port";
    private static final int DEFAULT_PORT = 65156;

    private static final String TRANSPORT_TYPE = "raft.server.transport.type";
    private static final String DEFAULT_TRANSPORT_TYPE = "netty";


    private static final String SNAPSHOT_DIRECTORY = "raft.server.snapshot.directory";
//    private static final String DEFAULT_SNAPSHOT_DIRECTORY = "netty";

    private static final String SNAPSHOT_NAME = "raft.server.snapshot.name";
    private static final String DEFAULT_SNAPSHOT_NAME = "charge";

    private static final String HEARTBEAT_INTERVAL = "raft.server.heartbeat.interval";
    private static final int DEFAULT_HEARTBEAT_INTERVAL = 3000;

    private static final String HEARTBEAT_TIMEOUT = "raft.server.heartbeat.timeout";
    private static final int DEFAULT_HEARTBEAT_TIMEOUT = 5000;

    private static final String MAX_ELECT_TIMEOUT = "raft.server.elect.timeout.max";
    private static final int DEFAULT_MAX_ELECT_TIMEOUT = 6000;

    private static final String MIN_ELECT_TIMEOUT = "raft.server.elect.timeout.min";
    private static final int DEFAULT_MIN_ELECT_TIMEOUT = 3000;

    private static final String MEMBERS = "raft.server.members";

    private static final String JOIN_RETRY_TIMES = "raft.server.join.retry.times";
    private static final int DEFAULT_JOIN_RETRY_TIMES = 6;

    private static final String MAX_APPEND_SIZE = "raft.server.append.size.max";
    private static final int DEFAULT_MAX_APPEND_SIZE = 5 * 1024 * 1024;

    private static final String LOG_NAME = "raft.server.log.name";
    private static final String DEFAULT_LOG_NAME = "raft";

    private static final String SERIALIZE_TYPE = "raft.server.log.serialize.type";
    private static final String DEFAULT_SERIALIZE_TYPE = "json";

    private static final String LOG_DIRECTORY = "raft.server.log.directory";

    private static final String LOG_FILE_MAX_SIZE = "raft.server.log.size.max";
    private static final int DEFAULT_LOG_FILE_MAX_SIZE = 64 * 1024 * 1024;

    private static final String LOG_ENTRY_MAX = "raft.server.log.entries.max";
    private static final int DEFAULT_LOG_ENTRY_MAX = 20 * 1000;

    private static final String COMMAND_MAX_SIZE = "raft.server.command.size.max";
    private static final int DEFAULT_COMMAND_MAX_SIZE = 1024 * 1024;


    public RaftConfig(Configure configure) {
        this.configure = configure;
    }

    public int getReplicateTimeout() {
        return configure.getInt(REPLICATE_TIMEOUT, DEFAULT_REPLICATE_TIMEOUT);
    }

    public String getClusterName() {
        return configure.getString(CLUSTER_NAME, DEFAULT_CLUSTER_NAME);
    }


    public String getTransportType() {
        return configure.getString(TRANSPORT_TYPE, DEFAULT_TRANSPORT_TYPE);
    }


    public int getPort() {
        return configure.getInt(PORT, DEFAULT_PORT);
    }


    public String getSnapshotDirectory() {
        return configure.getString(SNAPSHOT_DIRECTORY);
    }


    public String getSnapshotName() {
        return configure.getString(SNAPSHOT_NAME, DEFAULT_SNAPSHOT_NAME);
    }


    public int getHeartbeatTimeout() {

        return configure.getInt(HEARTBEAT_TIMEOUT, DEFAULT_HEARTBEAT_TIMEOUT);
    }


    public int getMaxElectTimeout() {
        return configure.getInt(MAX_ELECT_TIMEOUT, DEFAULT_MAX_ELECT_TIMEOUT);
    }


    public int getMinElectTimeout() {
        return configure.getInt(MIN_ELECT_TIMEOUT, DEFAULT_MIN_ELECT_TIMEOUT);
    }


    public String getMembers() {
        return configure.getString(MEMBERS);
    }


    public int getJoinRetryTimes() {
        return configure.getInt(JOIN_RETRY_TIMES, DEFAULT_JOIN_RETRY_TIMES);
    }


    public int getMaxAppendSize() {
        return configure.getInt(MAX_APPEND_SIZE, DEFAULT_MAX_APPEND_SIZE);
    }

    public int getHeartbeatInterval() {
        return configure.getInt(HEARTBEAT_INTERVAL, DEFAULT_HEARTBEAT_INTERVAL);
    }


    public String getSerializeType() {
        return configure.getString(SERIALIZE_TYPE, DEFAULT_SERIALIZE_TYPE);
    }

    public String getLogName() {
        return configure.getString(LOG_NAME, DEFAULT_LOG_NAME);
    }

    public String getLogDirectory() {
        return configure.getString(LOG_DIRECTORY);
    }


    public int getLogFileMaxSize() {
        return configure.getInt(LOG_FILE_MAX_SIZE, DEFAULT_LOG_FILE_MAX_SIZE);
    }


    public int getLogFileMaxEntries() {
        return configure.getInt(LOG_ENTRY_MAX, DEFAULT_LOG_ENTRY_MAX);
    }

    public int getCommandMaxSize() {
        return configure.getInt(COMMAND_MAX_SIZE, DEFAULT_COMMAND_MAX_SIZE);
    }
}
