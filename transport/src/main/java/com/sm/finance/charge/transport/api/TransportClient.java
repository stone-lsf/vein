package com.sm.finance.charge.transport.api;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.exceptions.ConnectException;

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
     * @throws ConnectException 异常
     */
    Connection connect(Address address) throws ConnectException;

    /**
     * 带重试次数的建立连接
     *
     * @param address    服务端地址
     * @param retryTimes 重试次数
     * @return Connection
     */
    Connection connect(Address address, int retryTimes);


    /**
     * 获取本地地址
     *
     * @return 本地地址
     */
    Address getLocalAddress();
}
