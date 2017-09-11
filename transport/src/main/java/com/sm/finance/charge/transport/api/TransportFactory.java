package com.sm.finance.charge.transport.api;

import com.sm.finance.charge.common.ServiceLoader;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:21
 */
public abstract class TransportFactory {

    public static Transport create(String transportType) {
        TransportFactory factory = ServiceLoader.findService(transportType, TransportFactory.class);
        return factory.doCreate();
    }

    public abstract Transport doCreate();
}
