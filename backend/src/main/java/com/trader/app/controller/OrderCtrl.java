package com.trader.app.controller;

import com.trader.app.entity.OrderEntity;
import com.trader.app.service.OrderService;
import com.trader.app.util.JwtUtil;
import com.trader.app.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderCtrl {

    @Autowired OrderService orderService;

    @PostMapping("/create")
    public Result<OrderEntity> create(@RequestHeader("Authorization") String token, @RequestBody Map<String, Long> body) {
        Long uid = JwtUtil.parseUserId(token);
        return Result.ok(orderService.createOrder(uid, body.get("prodId")));
    }

    @PostMapping("/pay")
    public Result<String> pay(@RequestHeader("Authorization") String token, @RequestBody Map<String, Long> body) {
        orderService.payOrder(JwtUtil.parseUserId(token), body.get("orderId"));
        return Result.ok("Paid");
    }

    @PostMapping("/ship")
    public Result<String> ship(@RequestHeader("Authorization") String token, @RequestBody Map<String, Long> body) {
        orderService.shipOrder(JwtUtil.parseUserId(token), body.get("orderId"));
        return Result.ok("Shipped");
    }

    @PostMapping("/receive")
    public Result<String> receive(@RequestHeader("Authorization") String token, @RequestBody Map<String, Long> body) {
        orderService.receiveOrder(JwtUtil.parseUserId(token), body.get("orderId"));
        return Result.ok("Received");
    }
}