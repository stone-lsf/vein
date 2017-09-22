package com.sm.finance.charge.cluster;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午1:23
 */
public class ClusterState {
    private ServerContext context;


    public long version() {
        return context.getMember().getState().getVersion();
    }
}
