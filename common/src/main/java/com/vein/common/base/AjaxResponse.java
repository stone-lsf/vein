package com.vein.common.base;

/**
 * @author shifeng.luo
 * @version created on 2017/11/18 下午10:19
 */
public class AjaxResponse<T> {
    public static final int SUCCESS = 0;

    private int status;

    private T data;

    public AjaxResponse() {
    }

    public AjaxResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    public AjaxResponse(int status) {
        this.status = status;
    }

    public static <R> AjaxResponse<R> success(R data) {
        return new AjaxResponse<>(SUCCESS, data);
    }

    public static <R> AjaxResponse<R> fail(int status) {
        return new AjaxResponse<>(status);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
