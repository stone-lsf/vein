package com.sm.finance.charge.transport.mock;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.AbstractConnection;
import com.sm.finance.charge.transport.api.Request;
import com.sm.finance.charge.transport.api.Response;

/**
 * @author shifeng.luo
 * @version created on 2017/10/23 上午11:53
 */
public class MockConnection extends AbstractConnection {

    private final String id;

    private MockNetwork network;

    MockConnection(Address localAddress, Address remoteAddress) {
        super(remoteAddress, localAddress, 3000);
        this.id = buildConnectionId(localAddress, remoteAddress);
    }


    @Override
    protected void doStart() throws Exception {

    }

    @Override
    public String getConnectionId() {
        return id;
    }

    @Override
    protected void sendRequest(Request request, int timeout) throws Exception {
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

    private String buildConnectionId(Address local, Address remote) {
        return local.getIp() + ":" + local.getPort() + "/" + remote.getIp() + ":" + remote.getPort();
    }

    public MockNetwork getNetwork() {
        return network;
    }

    public void setNetwork(MockNetwork network) {
        this.network = network;
    }
}

