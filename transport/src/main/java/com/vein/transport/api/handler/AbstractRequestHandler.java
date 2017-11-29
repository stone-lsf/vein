package com.vein.transport.api.handler;

import com.vein.common.base.LoggerSupport;
import com.vein.transport.api.support.HandleListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午6:39
 */
public abstract class AbstractRequestHandler<T> extends LoggerSupport implements RequestHandler<T> {
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
