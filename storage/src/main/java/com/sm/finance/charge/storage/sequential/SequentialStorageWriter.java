package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.LongIdGenerator;
import com.sm.finance.charge.storage.api.StorageWriter;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentAppender;
import com.sm.finance.charge.storage.api.segment.SegmentManager;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:41
 */
public class SequentialStorageWriter extends AbstractService implements StorageWriter {

    private final SegmentManager segmentManager;
    private final LongIdGenerator sequenceGenerator;
    private volatile SegmentAppender appender;

    public SequentialStorageWriter(SegmentManager segmentManager,long startSequence) {
        this.segmentManager = segmentManager;
        this.sequenceGenerator = new LongIdGenerator(startSequence);
    }

    @Override
    protected void doStart() throws Exception {
        Segment segment = segmentManager.last();
        if (segment == null) {
            segment = segmentManager.create(1);
        }
        this.appender = segment.appender();
    }

    @Override
    public void append(Object message) {

    }

    @Override
    public void append(List messages) {

    }

    @Override
    public void appendForce(Object message) {

    }

    @Override
    public void appendForce(List messages) {

    }

    @Override
    public void appendAsync(Object message) {

    }

    @Override
    public void appendAsync(List messages) {

    }


    @Override
    protected void doClose() {

    }


    private void appende(List<Object> messages){
        long sequence;
        for (Object message:messages) {
            sequence = sequenceGenerator.nextId();

        }

    }
}
