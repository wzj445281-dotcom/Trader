package com.trader.app.entity;
import lombok.Data;
@Data
public class PriceHistory {
    private Long id;
    private Long prodId;
    private Double price;
    private Long changedAt;
}
