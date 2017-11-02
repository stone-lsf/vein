package com.sm.charge.cluster.messages;

import com.sm.charge.cluster.group.Entry;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/11/2 下午10:37
 */
public class MessagePullResponse {

    /**
     * 已经提交的数据索引号
     */
    private long watermark;

    /**
     * 实际的数据负载
     */
    private List<Entry> entries;

    public long getWatermark() {
        return watermark;
    }

    public void setWatermark(long watermark) {
        this.watermark = watermark;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
}
