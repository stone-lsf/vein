package com.sm.finance.charge.transport.mock;

import com.sm.finance.charge.transport.api.Transport;
import com.sm.finance.charge.transport.api.TransportFactory;

/**
 * @author shifeng.luo
 * @version created on 2017/10/23 上午11:53
 */
public class MockTransportFactory extends TransportFactory {

    @Override
    public Transport doCreate() {
        return new MockTransport();
    }
}
