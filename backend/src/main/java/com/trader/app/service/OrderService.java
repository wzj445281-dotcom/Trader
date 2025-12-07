package com.trader.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trader.app.entity.*;
import com.trader.app.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired OrderMapper orderMapper;
    @Autowired OrderItemMapper orderItemMapper; // 新增
    @Autowired ProdMapper prodMapper;
    @Autowired CartItemMapper cartItemMapper;
    @Autowired NotificationMapper notificationMapper;

    /**
     * 从购物车批量创建订单 (报告 3.4 业务流程)
     * 涉及事务：创建订单/明细 -> 锁定库存 -> 清空购物车
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderEntity createOrderFromCart(Long userId, String address, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new IllegalArgumentException("请选择要结算的商品");
        }

        // 1. 获取购物车项
        List<CartItem> cartItems = cartItemMapper.selectBatchIds(cartItemIds);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("购物车数据异常");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Long sellerId = null; // 假设一次只能购买一个卖家的商品（简化逻辑）

        // 2. 创建订单主对象并插入
        OrderEntity order = new OrderEntity();
        order.setBuyerId(userId);
        order.setCreatedAt(System.currentTimeMillis());
        order.setStatus("CREATED");
        order.setAddress(address);
        orderMapper.insert(order);

        // 3. 遍历处理商品，创建订单明细
        for (CartItem ci : cartItems) {
            Prod p = prodMapper.selectById(ci.getProdId());
            if (p == null || !"AVAILABLE".equals(p.getStatus())) {
                throw new IllegalArgumentException("商品 [" + (p!=null?p.getTitle():ci.getProdId()) + "] 已下架或被抢走");
            }
            if (p.getUserId().equals(userId)) {
                throw new IllegalArgumentException("不能购买自己的商品");
            }

            if (sellerId == null) {
                sellerId = p.getUserId();
            }

            // 3.1 创建订单明细 (快照)
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProdId(p.getId());
            item.setProdName(p.getTitle());
            item.setProdImage(p.getImages() != null ? p.getImages().split(",")[0] : "");
            item.setPrice(BigDecimal.valueOf(p.getPrice()));
            item.setQuantity(ci.getQty());
            orderItemMapper.insert(item);

            // 3.2 累加金额
            totalAmount = totalAmount.add(BigDecimal.valueOf(p.getPrice()).multiply(new BigDecimal(ci.getQty())));

            // 3.3 锁定库存 (防止其他人在未支付前购买)
            p.setStatus("LOCKED");
            prodMapper.updateById(p);
        }

        // 4. 更新订单总价和卖家
        order.setTotalAmount(totalAmount);
        order.setSellerId(sellerId);
        orderMapper.updateById(order);

        // 5. 清除已结算的购物车项
        cartItemMapper.deleteBatchIds(cartItemIds);

        return order;
    }

    // 支付订单
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, Long orderId) {
        OrderEntity o = orderMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权操作");
        }
        if (!"CREATED".equals(o.getStatus())) {
            throw new IllegalArgumentException("订单状态不正确，无法支付");
        }

        o.setStatus("PAID");
        orderMapper.updateById(o);

        // 将 LOCKED 的商品改为 SOLD
        List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", orderId));
        for (OrderItem item : items) {
            Prod p = prodMapper.selectById(item.getProdId());
            if(p != null && "LOCKED".equals(p.getStatus())) {
                p.setStatus("SOLD");
                prodMapper.updateById(p);
            }
        }

        notify(o.getSellerId(), "订单已支付", "买家已付款，请尽快发货。订单号：" + orderId);
    }

    // 发货
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long userId, Long orderId) {
        OrderEntity o = orderMapper.selectById(orderId);
        if (o == null || !o.getSellerId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权操作");
        }
        if (!"PAID".equals(o.getStatus())) {
            throw new IllegalArgumentException("订单状态不正确，未支付无法发货");
        }

        o.setStatus("SHIPPED");
        orderMapper.updateById(o);

        notify(o.getBuyerId(), "订单已发货", "您的订单已发货，请注意查收。订单号：" + orderId);
    }

    // 确认收货 (完成)
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long userId, Long orderId) {
        OrderEntity o = orderMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权操作");
        }
        if (!"SHIPPED".equals(o.getStatus())) {
            throw new IllegalArgumentException("订单状态不正确，未发货无法确认收货");
        }

        o.setStatus("COMPLETED");
        orderMapper.updateById(o);

        notify(o.getSellerId(), "交易完成", "买家已确认收货，交易完成。订单号：" + orderId);
    }

    // 简单通知方法
    private void notify(Long uid, String title, String body) {
        if(uid == null) return;
        Notification n = new Notification();
        n.setUserId(uid);
        n.setTitle(title);
        n.setBody(body);
        n.setRead(false);
        n.setCreatedAt(System.currentTimeMillis());
        notificationMapper.insert(n);
    }
}