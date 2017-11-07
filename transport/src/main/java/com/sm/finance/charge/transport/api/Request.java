package com.sm.finance.charge.transport.api;

import com.sm.finance.charge.serializer.api.Serializable;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:06
 */
public class Request implements Serializable{
    private int id;

    private Object message;

    public Request() {
    }

    public Request(int id, Object message) {
        this.id = id;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Request{" +
            "id=" + id +
            ", message=" + message +
            '}';
    }
}
