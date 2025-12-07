package com.trader.app.entity;
import lombok.Data;
@Data
public class CartItem {
    private Long id;
    private Long userId;
    private Long prodId;
    private Integer qty;
}
