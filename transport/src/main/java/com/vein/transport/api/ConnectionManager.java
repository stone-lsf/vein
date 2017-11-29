package com.vein.transport.api;

import com.vein.transport.api.handler.RequestHandler;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:08
 */
public interface ConnectionManager {

    /**
     * 往所有连接中注册请求处理器
     *
     * @param handler 请求处理器
     */
    void registerMessageHandler(RequestHandler handler);

    /**
     * 添加连接，当server收到客户端的连接请求是调用
     *
     * @param connection 连接
     */
    void addConnection(Connection connection);

    /**
     * 获取连接
     *
     * @param connectionId 连接唯一标识符
     * @return 连接
     */
    Connection getConnection(String connectionId);

    /**
     * 删除连接
     *
     * @param connectionId 连接唯一标识符
     * @return 被删除的连接
     */
    Connection removeConnection(String connectionId);

    /**
     * 获取所有连接
     *
     * @return 连接列表
     */
    List<Connection> getAll();

    /**
     * 关闭所有连接
     */
    void closeAll() throws Exception;
}
