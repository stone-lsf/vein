package com.sm.finance.charge.transport.api;

import com.sm.finance.charge.common.base.Closable;
import com.sm.finance.charge.common.base.Startable;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:08
 */
public interface EndPoint extends Startable, Closable {

    /**
     * 获取连接管理器{@link ConnectionManager}
     *
     * @return 返回连接管理器
     */
    ConnectionManager getConnectionManager();

    /**
     * 带超时时间的关闭
     *
     * @param timeout 超时时间
     */
    void close(int timeout) throws Exception;

    /**
     * 判断终端是否关闭
     *
     * @return 如果关闭
     */
    boolean isClosed();
}
