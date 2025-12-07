package com.trader.app.util;

public class Consts {
    // 订单状态
    public static final String ORDER_CREATED = "CREATED";
    public static final String ORDER_PAID = "PAID";
    public static final String ORDER_SHIPPED = "SHIPPED";
    public static final String ORDER_COMPLETED = "COMPLETED";
    public static final String ORDER_CANCELLED = "CANCELLED";

    // 商品状态
    public static final String PROD_AVAILABLE = "AVAILABLE";
    public static final String PROD_LOCKED = "LOCKED"; // 下单未支付
    public static final String PROD_SOLD = "SOLD";
}