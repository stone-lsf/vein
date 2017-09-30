package com.sm.finance.charge.storage.api.rolling;

import com.sm.finance.charge.common.exceptions.BadDiskException;
import com.sm.finance.charge.storage.api.segment.Segment;
import com.sm.finance.charge.storage.api.segment.SegmentDescriptor;

/**
 * @author shifeng.luo
 * @version created on 2017/9/28 下午1:26
 */
public interface RollingPolicy {

    /**
     * 是否是触发文件切割的事件
     *
     * @param event 事件
     * @return 是则返回true，否则返回false
     */
    boolean isTriggerEvent(Event event);

    /**
     * 当需要切分文件的时候，需要对当前正在写入的文件做相应的善后处理，此时调用rollover方法
     */
    void rollover();

    /**
     * 获取下一个日志文件
     *
     * @return {@link Segment}
     */
    Segment nextSegment(SegmentDescriptor descriptor) throws BadDiskException;
}
