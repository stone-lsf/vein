package com.vein.raft.client.protocal;

/**
 * @author shifeng.luo
 * @version created on 2017/11/7 下午10:52
 */
public class RaftResponse<T> {
    public static final byte SUCCESS = 0;

    private byte status;

    private T data;

    public RaftResponse() {
    }

    public RaftResponse(byte status) {
        this.status = status;
    }

    public RaftResponse(byte status, T data) {
        this.status = status;
        this.data = data;
    }

    public static <R> RaftResponse<R> success(R data) {
        return new RaftResponse<>(SUCCESS, data);
    }

    public static <R> RaftResponse<R> fail(RaftError error) {
        return new RaftResponse<>(error.code);
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
