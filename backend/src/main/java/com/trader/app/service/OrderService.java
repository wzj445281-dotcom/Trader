package com.trader.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trader.app.entity.*;
import com.trader.app.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired OrderMapper orderMapper;
    @Autowired OrderItemMapper orderItemMapper;
    @Autowired ProdMapper prodMapper;
    @Autowired CartItemMapper cartItemMapper;
    @Autowired NotificationMapper notificationMapper;

    @Transactional(rollbackFor = Exception.class)
    public OrderEntity createOrderFromCart(Long userId, String address, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) throw new IllegalArgumentException("未选择商品");

        List<CartItem> cartItems = cartItemMapper.selectBatchIds(cartItemIds);
        if (cartItems.isEmpty()) throw new IllegalArgumentException("购物车数据异常");

        BigDecimal totalAmount = BigDecimal.ZERO;
        Long sellerId = null;

        OrderEntity order = new OrderEntity();
        order.setBuyerId(userId);
        order.setCreatedAt(System.currentTimeMillis());
        order.setStatus("CREATED");
        order.setAddress(address);
        orderMapper.insert(order);

        for (CartItem ci : cartItems) {
            Prod p = prodMapper.selectById(ci.getProdId());
            if (p == null || !"AVAILABLE".equals(p.getStatus())) {
                // 记录日志，便于调试
                System.err.println("结算失败：商品 [" + (p != null ? p.getTitle() : ci.getProdId()) + "] 状态不可用或不存在");
                throw new IllegalArgumentException("商品 [" + (p != null ? p.getTitle() : ci.getProdId()) + "] 无法购买");
            }

            // 核心校验：用户不能购买自己的商品
            if (p.getUserId().equals(userId)) {
                // 记录日志，便于调试
                System.err.println("结算失败：用户 " + userId + " 试图购买自己的商品 " + p.getId());
                throw new IllegalArgumentException("不能购买自己的商品: [" + p.getTitle() + "]");
            }

            // 如果是多件商品，需要确保它们来自同一个卖家。此处逻辑是第一次循环时确定 sellerId
            if (sellerId == null) sellerId = p.getUserId();

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProdId(p.getId());
            item.setProdName(p.getTitle());
            item.setProdImage(p.getImages() != null ? p.getImages().split(",")[0] : "");
            item.setPrice(BigDecimal.valueOf(p.getPrice()));
            item.setQuantity(ci.getQty());
            orderItemMapper.insert(item);

            totalAmount = totalAmount.add(BigDecimal.valueOf(p.getPrice()).multiply(new BigDecimal(ci.getQty())));

            // 扣减库存
            int currentStock = p.getStock() == null ? 0 : p.getStock();
            if (currentStock < ci.getQty()) {
                // 记录日志，便于调试
                System.err.println("结算失败：商品 [" + p.getTitle() + "] 库存不足");
                throw new IllegalArgumentException("商品 [" + p.getTitle() + "] 库存不足");
            }
            p.setStock(currentStock - ci.getQty());
            if (p.getStock() <= 0) p.setStatus("LOCKED");
            prodMapper.updateById(p);
        } // for 循环结束

        // 最终更新订单总价和卖家 ID
        order.setTotalAmount(totalAmount);
        order.setSellerId(sellerId);
        orderMapper.updateById(order);

        // 清空购物车项
        cartItemMapper.deleteBatchIds(cartItemIds);
        return order; // 正常返回订单实体
    }

    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, Long orderId) {
        OrderEntity o = orderMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(userId)) throw new IllegalArgumentException("无权操作");
        if (!"CREATED".equals(o.getStatus())) throw new IllegalArgumentException("订单状态异常");

        o.setStatus("PAID");
        orderMapper.updateById(o);

        // 将关联商品标记为 SOLD (如果库存已扣完)
        List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", orderId));
        for (OrderItem item : items) {
            Prod p = prodMapper.selectById(item.getProdId());
            if(p != null && p.getStock() <= 0) {
                p.setStatus("SOLD");
                prodMapper.updateById(p);
            }
        }
        notify(o.getSellerId(), "订单已支付", "订单号：" + orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long userId, Long orderId) {
        OrderEntity o = orderMapper.selectById(orderId);
        if (o == null || !o.getSellerId().equals(userId)) throw new IllegalArgumentException("无权操作");
        o.setStatus("SHIPPED");
        orderMapper.updateById(o);
        notify(o.getBuyerId(), "订单已发货", "订单号：" + orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long userId, Long orderId) {
        OrderEntity o = orderMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(userId)) throw new IllegalArgumentException("无权操作");
        o.setStatus("COMPLETED");
        orderMapper.updateById(o);
        notify(o.getSellerId(), "交易完成", "订单号：" + orderId);
    }

    // 取消订单
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        OrderEntity o = orderMapper.selectById(orderId);
        if (o == null) throw new IllegalArgumentException("订单不存在");

        // 只有买家或卖家可以取消
        if (!o.getBuyerId().equals(userId) && !o.getSellerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }

        // 只有 CREATED 状态可以取消 (已支付的需要走退款流程，这里简化)
        if (!"CREATED".equals(o.getStatus())) {
            throw new IllegalArgumentException("当前状态无法取消");
        }

        o.setStatus("CANCELLED");
        orderMapper.updateById(o);

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", orderId));
        for (OrderItem item : items) {
            Prod p = prodMapper.selectById(item.getProdId());
            if (p != null) {
                p.setStock(p.getStock() + item.getQuantity());
                if ("LOCKED".equals(p.getStatus())) {
                    p.setStatus("AVAILABLE");
                }
                prodMapper.updateById(p);
            }
        }

        notify(o.getSellerId().equals(userId) ? o.getBuyerId() : o.getSellerId(), "订单已取消", "订单号：" + orderId);
    }

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