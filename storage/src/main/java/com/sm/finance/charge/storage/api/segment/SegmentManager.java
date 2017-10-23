package com.sm.finance.charge.storage.api.segment;

import com.sm.finance.charge.common.base.Closable;
import com.sm.finance.charge.common.base.Startable;

import java.io.IOException;
import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:31
 */
public interface SegmentManager extends Startable, Closable {

    Segment create(long sequence) throws IOException;

    boolean delete(long sequence);

    Segment get(long sequence);

    Segment last();

    List<Segment> higher(long sequence);
}
