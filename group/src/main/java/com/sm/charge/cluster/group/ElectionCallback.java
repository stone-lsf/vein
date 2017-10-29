package com.sm.charge.cluster.group;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午9:18
 */
public interface ElectionCallback {

    void onElectAsLeader();

    void onFailure(Throwable error);
}
