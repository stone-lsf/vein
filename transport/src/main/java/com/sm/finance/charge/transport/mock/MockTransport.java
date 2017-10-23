package com.sm.finance.charge.transport.mock;

import com.sm.finance.charge.transport.api.Transport;
import com.sm.finance.charge.transport.api.TransportClient;
import com.sm.finance.charge.transport.api.TransportServer;

/**
 * @author shifeng.luo
 * @version created on 2017/10/23 上午11:53
 */
public class MockTransport implements Transport {
    @Override
    public TransportClient client() {
        return new MockClient();

    }

    @Override
    public TransportServer server() {
        return new MockServer();
    }
}
