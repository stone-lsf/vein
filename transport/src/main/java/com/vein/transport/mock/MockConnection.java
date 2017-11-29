package com.vein.transport.mock;

import com.vein.common.Address;
import com.vein.transport.api.AbstractConnection;
import com.vein.transport.api.Request;
import com.vein.transport.api.Response;

/**
 * @author shifeng.luo
 * @version created on 2017/10/23 上午11:53
 */
public class MockConnection extends AbstractConnection {

    private MockNetwork network;

    MockConnection(Address localAddress, Address remoteAddress) {
        super(remoteAddress, localAddress, 3000);
    }


    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void sendAndReceive(Request request, int timeout) throws Exception {
        network.sendRequest(this, request);
        timeoutScheduler.schedule(request.getId(), timeout);
    }

    @Override
    protected void sendRequest(Request request) throws Exception {
        network.sendRequest(this, request);
    }

    @Override
    protected void sendResponse(Response response) throws Exception {
        network.sendResponse(this, response);
    }

    @Override
    protected void doClose() throws Exception {
        super.doClose();
        network.removeConnection(this);
    }

    void setNetwork(MockNetwork network) {
        this.network = network;
    }
}

