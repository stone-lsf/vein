package com.sm.finance.charge.transport.api.handler;

import com.sm.finance.charge.transport.api.support.HandleListener;
import com.sm.finance.charge.transport.api.support.RequestContext;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:33
 */
public interface RequestHandler<T> {

    /**
     * 处理接收到的消息
     *
     * @param message 消息
     * @param context 请求上下文
     * @return 返回结果，可以为null
     * @throws Exception 异常
     */
    Object handle(final T message, RequestContext context) throws Exception;

    /**
     * 新增处理监听器
     *
     * @param listener 监听器
     */
    void add(HandleListener listener);

    /**
     * 获取监听器列表
     *
     * @return 监听器列表
     */
    List<HandleListener> getAllListeners();
}