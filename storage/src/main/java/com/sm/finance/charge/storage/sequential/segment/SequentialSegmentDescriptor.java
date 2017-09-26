package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.storage.api.segment.SegmentDescriptor;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 下午2:01
 */
public class SequentialSegmentDescriptor implements SegmentDescriptor {

    private final long sequence;
    private volatile boolean locked;
    private volatile boolean updated;

    public SequentialSegmentDescriptor(long sequence) {
        this.sequence = sequence;
    }

    @Override
    public long sequence() {
        return sequence;
    }

    @Override
    public boolean locked() {
        return locked;
    }

    @Override
    public boolean updated() {
        return updated;
    }
}
