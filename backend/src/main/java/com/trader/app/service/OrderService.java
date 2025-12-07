package com.trader.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trader.app.entity.Notification;
import com.trader.app.entity.OrderEntity;
import com.trader.app.entity.Prod;
import com.trader.app.mapper.NotificationMapper;
import com.trader.app.mapper.OrderMapper;
import com.trader.app.mapper.ProdMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired OrderMapper orderMapper;
    @Autowired ProdMapper prodMapper;
    @Autowired NotificationMapper notificationMapper;

    // 创建订单
    @Transactional
    public OrderEntity createOrder(Long buyerId, Long prodId) {
        Prod p = prodMapper.selectById(prodId);
        if (p == null || !"AVAILABLE".equals(p.getStatus())) {
            throw new IllegalArgumentException("商品不存在或已被抢走");
        }
        if (p.getUserId().equals(buyerId)) {
            throw new IllegalArgumentException("不能购买自己的商品");
        }

        OrderEntity o = new OrderEntity();
        o.setBuyerId(buyerId);
        o.setSellerId(p.getUserId());
        o.setProdId(prodId);
        o.setPrice(p.getPrice());
        o.setStatus("CREATED"); // 待支付
        o.setCreatedAt(System.currentTimeMillis());
        orderMapper.insert(o);

        // 锁定商品状态
        p.setStatus("LOCKED");
        prodMapper.updateById(p);

        return o;
    }

    // 支付订单
    @Transactional
    public void payOrder(Long userId, Long orderId) {
        OrderEntity o = orderMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(userId)) throw new IllegalArgumentException("无权操作");
        if (!"CREATED".equals(o.getStatus())) throw new IllegalArgumentException("订单状态不正确");

        o.setStatus("PAID"); // 已支付，待发货
        orderMapper.updateById(o);

        // 只有支付成功才真正标记商品售出
        Prod p = prodMapper.selectById(o.getProdId());
        p.setStatus("SOLD");
        prodMapper.updateById(p);

        notify(o.getSellerId(), "订单已支付", "买家已付款，请尽快发货。订单号：" + orderId);
    }

    // 发货
    public void shipOrder(Long userId, Long orderId) {
        OrderEntity o = orderMapper.selectById(orderId);
        if (o == null || !o.getSellerId().equals(userId)) throw new IllegalArgumentException("无权操作");

        o.setStatus("SHIPPED"); // 已发货
        orderMapper.updateById(o);
        notify(o.getBuyerId(), "卖家已发货", "您的宝贝已经在路上了，订单号：" + orderId);
    }

    // 确认收货
    public void receiveOrder(Long userId, Long orderId) {
        OrderEntity o = orderMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(userId)) throw new IllegalArgumentException("无权操作");

        o.setStatus("COMPLETED"); // 交易完成
        orderMapper.updateById(o);
        notify(o.getSellerId(), "交易成功", "买家已确认收货，钱款已到账。");
    }

    // 查询我的订单（买家+卖家）
    public List<OrderEntity> getMyOrders(Long userId) {
        QueryWrapper<OrderEntity> q = new QueryWrapper<>();
        q.eq("buyer_id", userId).or().eq("seller_id", userId);
        q.orderByDesc("created_at");
        return orderMapper.selectList(q);
    }

    private void notify(Long uid, String title, String body) {
        Notification n = new Notification();
        n.setUserId(uid);
        n.setTitle(title);
        n.setBody(body);
        n.setRead(false);
        n.setCreatedAt(System.currentTimeMillis());
        notificationMapper.insert(n);
    }
}