package com.sm.charge.cluster.group;

/**
 * @author shifeng.luo
 * @version created on 2017/11/2 下午10:47
 */
public interface LeaderListener {

    void onLeave(Server leader);

    void onSelected(Server leader);
}
