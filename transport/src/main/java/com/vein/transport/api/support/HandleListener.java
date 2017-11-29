package com.vein.transport.api.support;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:37
 */
public interface HandleListener {

    /**
     * 当处理成功时，调用此方法
     */
    void onSuccess();

    /**
     * 当处理失败，抛出异常时，调用此方法
     *
     * @param e 异常
     */
    void onFail(Throwable e);
}
