package com.sm.finance.charge.cluster;

/**
 * @author shifeng.luo
 * @version created on 2017/10/8 下午2:20
 */
public interface Compactor {

    void updateCompactIndex(long index);

    long nextCompactIndex();
}
