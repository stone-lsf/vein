package com.vein.transport.api;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:12
 */
public interface ConnectionListener {

    /**
     * 处理监听到的新建的连接
     *
     * @param connection 连接{@link Connection}
     */
    void onConnect(Connection connection);
}
