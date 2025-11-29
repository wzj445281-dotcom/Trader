package com.trader.app.entity;
import lombok.Data;
@Data
public class OrderEntity {
    private Long id;
    private Long buyerId;
    private Long sellerId;
    private Long prodId;
    private String status; // CREATED, PAID, ESCROW, COMPLETED, CANCELLED
    private Double price;
    private Long createdAt;
}
