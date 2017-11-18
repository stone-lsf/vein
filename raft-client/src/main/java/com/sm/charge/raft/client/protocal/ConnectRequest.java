package com.sm.charge.raft.client.protocal;

/**
 * @author shifeng.luo
 * @version created on 2017/11/18 上午11:57
 */
public class ConnectRequest {

    private String clientId;

    public ConnectRequest() {
    }

    public ConnectRequest(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
