package com.trader.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单明细实体 (对应 OrderItem 表)
 */
@Data
public class OrderItem {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;      // 关联的订单ID
    private Long prodId;       // 商品ID
    private String prodName;   // 商品名称快照 (防止商品改名后订单信息错误)
    private String prodImage;  // 商品图片快照
    private BigDecimal price;  // 下单时的单价快照
    private Integer quantity;  // 购买数量
}