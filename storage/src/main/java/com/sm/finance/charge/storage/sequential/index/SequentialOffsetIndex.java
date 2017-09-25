package com.sm.finance.charge.storage.sequential.index;

import com.sm.finance.charge.storage.api.index.OffsetIndex;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:54
 */
public class SequentialOffsetIndex implements OffsetIndex {

    private long sequence;
    private long offset;

    @Override
    public long sequence() {
        return sequence;
    }

    @Override
    public long offset() {
        return offset;
    }
}
