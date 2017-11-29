package com.vein.transport.api.support;

import com.vein.common.Address;
import com.vein.transport.api.Connection;
import com.vein.transport.api.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:34
 */
public class RequestContext {
    private static final Logger logger = LoggerFactory.getLogger(RequestContext.class);

    private int requestId;

    private Connection connection;

    private Address remote;


    public RequestContext(int requestId, Connection connection, Address remote) {
        this.requestId = requestId;
        this.connection = connection;
        this.remote = remote;
    }

    public void sendResponse(Object response) throws IOException {
        Response rsp = new Response(requestId, response);
        connection.send(rsp);
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Address getRemote() {
        return remote;
    }

    public void setRemote(Address remote) {
        this.remote = remote;
    }
}
