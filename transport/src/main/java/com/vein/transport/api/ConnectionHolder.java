package com.vein.transport.api;

import com.vein.common.base.CloseListener;

/**
 * @author shifeng.luo
 * @version created on 2017/11/10 下午9:56
 */
public interface ConnectionHolder extends CloseListener {

    Connection getConnection();

    void setConnection(Connection connection);

    void clearConnection();

    /**
     * close
     */
    @Override
    default void onClose() {
        clearConnection();
    }
}
