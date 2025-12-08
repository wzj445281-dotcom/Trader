package com.trader.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trader.app.entity.OrderEntity;
import com.trader.app.entity.OrderItem;
import com.trader.app.mapper.OrderItemMapper;
import com.trader.app.mapper.OrderMapper;
import com.trader.app.service.OrderService;
import com.trader.app.util.JwtUtil;
import com.trader.app.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order")
public class OrderCtrl {

    @Autowired OrderService orderService;
    @Autowired OrderMapper orderMapper;
    @Autowired OrderItemMapper orderItemMapper;

    @PostMapping("/create")
    public Result<OrderEntity> create(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> body) {
        Long uid = JwtUtil.parseUserId(token);
        String address = (String) body.get("address");
        List<Integer> ids = (List<Integer>) body.get("cartItemIds");
        List<Long> longIds = ids.stream().map(Long::valueOf).collect(Collectors.toList());
        return Result.ok(orderService.createOrderFromCart(uid, address, longIds));
    }

    @PostMapping("/pay/{orderId}")
    public Result<String> pay(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        Long uid = JwtUtil.parseUserId(token);
        orderService.payOrder(uid, orderId);
        return Result.ok("支付成功");
    }

    @PostMapping("/ship/{orderId}")
    public Result<String> ship(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        Long uid = JwtUtil.parseUserId(token);
        orderService.shipOrder(uid, orderId);
        return Result.ok("发货成功");
    }

    @PostMapping("/complete/{orderId}")
    public Result<String> complete(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        Long uid = JwtUtil.parseUserId(token);
        orderService.completeOrder(uid, orderId);
        return Result.ok("确认收货成功");
    }

    // 🔥 新增：取消订单接口
    @PostMapping("/cancel/{orderId}")
    public Result<String> cancel(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        Long uid = JwtUtil.parseUserId(token);
        orderService.cancelOrder(uid, orderId);
        return Result.ok("订单已取消");
    }

    @GetMapping("/my")
    public Result<List<OrderEntity>> myOrders(@RequestHeader("Authorization") String token) {
        Long uid = JwtUtil.parseUserId(token);
        QueryWrapper<OrderEntity> q = new QueryWrapper<>();
        q.eq("buyer_id", uid).or().eq("seller_id", uid);
        q.orderByDesc("created_at");
        List<OrderEntity> orders = orderMapper.selectList(q);
        for (OrderEntity o : orders) {
            List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", o.getId()));
            o.setItems(items);
        }
        return Result.ok(orders);
    }
}