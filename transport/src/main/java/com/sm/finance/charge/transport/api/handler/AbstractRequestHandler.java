package com.sm.finance.charge.transport.api.handler;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.transport.api.support.HandleListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午6:39
 */
public abstract class AbstractRequestHandler<T> extends LogSupport implements RequestHandler<T> {
    private List<HandleListener> listeners = new ArrayList<>();

    @Override
    public void add(HandleListener listener) {
        listeners.add(listener);
    }

    @Override
    public List<HandleListener> getAllListeners() {
        return new ArrayList<>(listeners);
    }
}
