package com.sm.charge.raft.server.membership;

import com.sm.finance.charge.common.Address;

/**
 * @author shifeng.luo
 * @version created on 2017/10/11 下午1:47
 */
public class JoinResponse {

    /**
     * 状态码
     */
    private int status;

    private Address master;

//    /**
//     * 集群服务器列表
//     */
//    private List<ClusterServer> servers;

//    private boolean needInstallSnapshot;
}
