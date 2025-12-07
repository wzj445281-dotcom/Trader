package com.trader.app.controller;

import com.trader.app.entity.OrderEntity;
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

    // 升级：支持从购物车批量下单
    @PostMapping("/create")
    public Result<OrderEntity> create(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> body) {
        Long uid = JwtUtil.parseUserId(token);
        String address = (String) body.get("address");
        // 从 JSON 中获取 List<Integer>
        List<Integer> ids = (List<Integer>) body.get("cartItemIds");

        // 转换类型 List<Integer> to List<Long>
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

    // TODO: 完善获取订单列表、订单详情的接口，以便个人中心调用。
}