package com.vein.cluster.group;

import com.vein.cluster.ClusterMessage;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 上午12:20
 */
public class Entry {

    private long index;

    private ClusterMessage message;

    public Entry() {
    }

    public Entry(ClusterMessage message) {
        this.message = message;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public ClusterMessage getMessage() {
        return message;
    }

    public void setMessage(ClusterMessage message) {
        this.message = message;
    }
}
