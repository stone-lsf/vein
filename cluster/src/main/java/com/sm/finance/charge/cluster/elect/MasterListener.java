package com.sm.finance.charge.cluster.elect;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午3:17
 */
public interface MasterListener {

    void onMaster();

    void offMaster();
}
