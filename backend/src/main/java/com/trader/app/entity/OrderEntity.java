package com.trader.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单主实体 (对应 Orders 表)
 */
@Data
public class OrderEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long buyerId;
    private Long sellerId;

    private String status; // CREATED, PAID, SHIPPED, COMPLETED, CANCELLED
    private BigDecimal totalAmount; // 订单总金额
    private String address; // 收货地址
    private Long createdAt;

    // 用于前端展示的非数据库字段：订单明细列表
    @TableField(exist = false)
    private List<OrderItem> items;
}