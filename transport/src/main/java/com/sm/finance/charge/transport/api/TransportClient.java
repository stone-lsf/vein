package com.sm.finance.charge.transport.api;

import com.sm.finance.charge.common.Address;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:09
 */
public interface TransportClient extends EndPoint {

    /**
     * 建立连接
     *
     * @param address 服务的地址
     * @return Connection
     */
    CompletableFuture<Connection> connect(Address address);

    /**
     * 带重试次数的建立连接
     *
     * @param address    服务端地址
     * @param retryTimes 重试次数
     * @return Connection
     */
    CompletableFuture<Connection> connect(Address address, int retryTimes);

}
