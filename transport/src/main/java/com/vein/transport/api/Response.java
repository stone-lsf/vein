package com.vein.transport.api;

import com.vein.serializer.api.Serializable;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:07
 */
public class Response implements Serializable {
    public static final Object EMPTY_MESSAGE = new Object();

    private int id;

    private Object message;

    private Exception exception;

    public Response(int id) {
        this.id = id;
    }

    public Response(int id, Object message) {
        this.id = id;
        this.message = message;
    }

    public Response(int id, Exception exception) {
        this.id = id;
        this.exception = exception;
    }

    public boolean hasException() {
        return exception != null;
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

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "Response{" +
            "id=" + id +
            ", message=" + message +
            ", exception=" + exception +
            '}';
    }
}
