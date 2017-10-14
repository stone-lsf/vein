package com.sm.finance.charge.common;

import com.google.common.base.Preconditions;

import java.net.InetSocketAddress;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:16
 */
public class Address {
    private String ip;
    private int port;

    public Address(InetSocketAddress socketAddress) {
        Preconditions.checkNotNull(socketAddress);
        this.ip = socketAddress.getAddress().getHostAddress();
        this.port = socketAddress.getPort();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    /**
     * 返回ip port的long型数据
     * 127.0.0.1:2001---> 1270012001
     *
     * @return long
     */
    public long ipPort() {
        String ipPort = ip + port;
        ipPort = ipPort.replace(".", "");
        return Long.valueOf(ipPort);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof Address)) {
            return false;
        }

        Address address = (Address) object;
        return address.ip.equals(this.ip) && this.port == address.port;
    }

    @Override
    public int hashCode() {
        return ip == null ? port : ip.hashCode() + port;
    }

    @Override
    public String toString() {
        return "Address{" +
            "ip='" + ip + '\'' +
            ", port=" + port +
            '}';
    }
}
