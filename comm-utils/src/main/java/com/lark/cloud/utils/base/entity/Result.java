package com.lark.cloud.utils.base.entity;


/**
 * 统一结果对象
 * @param <R>
 * @date 2018-12
 * @author xc.li
 */
public class Result<R> {

    private int status;
    private String message;
    private R data;

    public Result(){}

    public Result(Status status) {
        this.status = status.getStatus();
        this.message = status.getMessage();
    }

    public Result(Status status, R data){
        this.setStatus(status);
        this.setData(data);
    }

    public R getData() {
        return data;
    }

    public void setData(R data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(Status status) {
        this.status = status.getStatus();
        this.message = status.getMessage();
    }
}
