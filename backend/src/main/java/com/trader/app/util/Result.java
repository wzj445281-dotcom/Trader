package com.trader.app.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    // 成功：有数据
    public static <T> Result<T> ok(T d){
        return new Result<>(0, "ok", d);
    }

    // 成功：无数据（你当前缺少的就是这个！）
    public static <T> Result<T> ok(){
        return new Result<>(0, "ok", null);
    }

    // 失败
    public static <T> Result<T> fail(String m){
        return new Result<>(-1, m, null);
    }
}
