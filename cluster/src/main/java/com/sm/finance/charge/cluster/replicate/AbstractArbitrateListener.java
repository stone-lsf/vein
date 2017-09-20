package com.sm.finance.charge.cluster.replicate;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午4:44
 */
public abstract class AbstractArbitrateListener implements ArbitrateListener {

    private ReplicateArbitratorManager arbitratorManager;
    private ReplicateData replicateData;

    protected AbstractArbitrateListener(ReplicateArbitratorManager arbitratorManager, ReplicateData replicateData) {
        this.arbitratorManager = arbitratorManager;
        this.replicateData = replicateData;
    }

    @Override
    public void onSuccess() {
        arbitratorManager.remove(replicateData.getId());
    }

    @Override
    public void onFail() {
        arbitratorManager.remove(replicateData.getId());
    }
}
