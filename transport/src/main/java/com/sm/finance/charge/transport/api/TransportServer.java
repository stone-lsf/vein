package com.sm.finance.charge.transport.api;

import com.sm.finance.charge.transport.api.exceptions.BindException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:14
 */
public interface TransportServer extends EndPoint {

    /**
     * 绑定本地端口，并设置客户端连接监听器
     *
     * @param port     端口号
     * @param listener 连接监听器
     * @throws BindException 当绑定端口失败时，抛出异常
     */
    void listen(int port, ConnectionListener listener) throws BindException;
}
