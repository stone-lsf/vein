package com.sm.finance.charge.cluster.replicate;

import com.sm.finance.charge.common.NamedThreadFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shifeng.luo
 * @version created on 2017/9/20 下午4:03
 */
public class ReplicateArbitratorManager {
    private ConcurrentMap<String, ReplicateArbitrator> arbitratorMap = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool(new NamedThreadFactory("ReplicatePool"));

    public void add(ReplicateData data, ReplicateArbitrator arbitrator) {
        ReplicateArbitrator exist = arbitratorMap.putIfAbsent(data.getId(), arbitrator);
        if (exist == null) {
            executorService.execute(arbitrator);
        }
    }

    public void remove(String dataId) {
        arbitratorMap.remove(dataId);
    }
}
