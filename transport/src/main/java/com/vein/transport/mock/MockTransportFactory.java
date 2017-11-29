package com.vein.transport.mock;

import com.vein.transport.api.Transport;
import com.vein.transport.api.TransportFactory;

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
