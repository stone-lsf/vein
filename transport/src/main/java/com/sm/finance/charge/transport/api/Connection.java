package com.sm.finance.charge.transport.api;

import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.Closable;
import com.sm.finance.charge.common.CloseListener;
import com.sm.finance.charge.common.Startable;
import com.sm.finance.charge.transport.api.handler.RequestHandler;
import com.sm.finance.charge.transport.api.handler.ResponseHandler;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:12
 */
public interface Connection extends Startable, Closable {

    /**
     * 返回连接唯一标识符
     *
     * @return 连接唯一标识符
     */
    String getConnectionId();

    /**
     * 注册请求处理器
     *
     * @param handlers 请求处理器
     */
    void registerHandlers(List<RequestHandler> handlers);

    /**
     * 发送消息，该消息不会产生返回结果
     *
     * @param message 消息
     */
    void send(Object message);


    /**
     * 异步发送消息，此时使用默认的超时时间
     *
     * @param message 消息
     * @param handler 返回结果处理器
     */
    <T> void send(Object message, ResponseHandler<T> handler);

    /**
     * 异步发送消息
     *
     * @param message         消息
     * @param timeout         超时时间
     * @param handler 返回结果处理器
     */
    <T> void send(Object message, int timeout, ResponseHandler<T> handler);

    /**
     * 发送消息
     *
     * @param message 消息
     * @return {@link CompletableFuture}，用来阻塞直到有消息返回或者超时
     */
    <T> CompletableFuture<T> request(Object message);

    /**
     * 发送消息
     *
     * @param message 消息
     * @param timeout 超时时间
     * @return {@link CompletableFuture}，用来阻塞直到有消息返回或者超时
     */
    <T> CompletableFuture<T> request(Object message, int timeout);

    /**
     * 同步发送请求，使用默认超时时间，会阻塞一直等待返回结果或超时
     *
     * @param message 请求消息
     * @return 结果
     */
    <T> T syncRequest(Object message) throws Exception;

    /**
     * 同步发送请求，并指定超时时间，会阻塞一直等待返回结果或超时
     *
     * @param message 请求消息
     * @return 结果
     */
    <T> T syncRequest(Object message, int timeout) throws Exception;

    /**
     * 处理收到的消息
     *
     * @param message 消息
     */
    void onMessage(Object message);

    /**
     * 设置关闭监听器，当{@link Connection}关闭时，会触发该监听器
     *
     * @param listener 关闭监听器
     */
    void addCloseListener(CloseListener listener);

    /**
     * 连接是否关闭
     *
     * @return 如果关闭则返回true，否则返回false
     */
    boolean closed();

    /**
     * 获取本地地址
     * @return {@link Address}
     */
    Address localAddress();

    /**
     * 获取远程地址
     * @return {@link Address}
     */
    Address remoteAddress();
}
