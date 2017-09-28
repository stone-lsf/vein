package com.sm.finance.charge.storage.api.segment;

import com.sm.finance.charge.common.Closable;
import com.sm.finance.charge.common.Startable;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:31
 */
public interface SegmentManager extends Startable, Closable {

    Segment create(long sequence);

    Segment last();
}
