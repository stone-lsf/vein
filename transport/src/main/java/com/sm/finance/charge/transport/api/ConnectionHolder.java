package com.sm.finance.charge.transport.api;

import com.sm.finance.charge.common.base.CloseListener;

/**
 * @author shifeng.luo
 * @version created on 2017/11/10 下午9:56
 */
public interface ConnectionHolder extends CloseListener {

    Connection getConnection();

    void setConnection(Connection connection);

    void clearConnection();

    default void onClose() {
        clearConnection();
    }
}
