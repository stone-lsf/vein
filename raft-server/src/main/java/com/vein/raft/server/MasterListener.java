package com.vein.raft.server;

/**
 * @author shifeng.luo
 * @version created on 2017/10/10 下午10:45
 */
public interface MasterListener {

    void onMaster();

    void offMaster();
}
