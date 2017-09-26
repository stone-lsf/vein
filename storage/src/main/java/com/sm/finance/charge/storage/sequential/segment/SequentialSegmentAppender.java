package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentAppender;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:54
 */
public class SequentialSegmentAppender implements SegmentAppender {
    @Override
    public Segment getSegment() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> write(Entry entry) {
        return null;
    }

    @Override
    public SegmentAppender truncate(long offset) {
        return null;
    }

    @Override
    public SegmentAppender flush() {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
