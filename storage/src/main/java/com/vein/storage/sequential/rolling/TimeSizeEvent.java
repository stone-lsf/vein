package com.vein.storage.sequential.rolling;

import com.vein.storage.api.rolling.Event;

import java.util.Date;

/**
 * @author shifeng.luo
 * @version created on 2017/9/29 下午3:43
 */
public class TimeSizeEvent implements Event {
    private long size;

    private Date currentTime;

    public TimeSizeEvent() {
    }

    public TimeSizeEvent(long size, Date currentTime) {
        this.size = size;
        this.currentTime = currentTime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }
}
