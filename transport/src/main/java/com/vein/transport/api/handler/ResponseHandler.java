package com.vein.transport.api.handler;

import com.vein.transport.api.Connection;
import com.vein.transport.api.support.ResponseContext;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:39
 */
public interface ResponseHandler<T> {

    /**
     * 处理响应结果
     *
     * @param response   结果
     * @param connection 连接
     */
    void handle(T response, Connection connection);

    /**
     * 当响应出现异常时，进行处理
     *
     * @param e       异常
     * @param context 响应上下文
     */
    void onException(Throwable e, ResponseContext context);
}
