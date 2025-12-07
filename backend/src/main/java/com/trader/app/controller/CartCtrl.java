package com.trader.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trader.app.entity.CartItem;
import com.trader.app.entity.Prod;
import com.trader.app.mapper.CartItemMapper;
import com.trader.app.mapper.ProdMapper;
import com.trader.app.util.JwtUtil;
import com.trader.app.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prod/cart")
public class CartCtrl {

    @Autowired
    CartItemMapper cartItemMapper;

    @Autowired
    ProdMapper prodMapper;

    // 获取购物车列表（包含商品详情）
    @GetMapping("/{userId}")
    public Result<List<Map<String, Object>>> getCart(@PathVariable Long userId, @RequestHeader("Authorization") String token) {
        // 安全校验：确保操作的是自己的购物车
        Long currentUid = JwtUtil.parseUserId(token);
        if (currentUid == null || !currentUid.equals(userId)) {
            return Result.fail("Unauthorized access to cart");
        }

        QueryWrapper<CartItem> q = new QueryWrapper<>();
        q.eq("user_id", userId);
        List<CartItem> items = cartItemMapper.selectList(q);

        List<Map<String, Object>> result = new ArrayList<>();
        for (CartItem item : items) {
            Prod p = prodMapper.selectById(item.getProdId());
            if (p != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", item.getId());
                map.put("prodId", p.getId());
                map.put("prodTitle", p.getTitle());
                map.put("prodPrice", p.getPrice());
                map.put("prodImage", p.getImages() != null ? p.getImages().split(",")[0] : "");
                map.put("qty", item.getQty());
                result.add(map);
            }
        }
        return Result.ok(result);
    }

    // 添加到购物车
    @PostMapping("/add")
    public Result<String> add(@RequestBody CartItem item, @RequestHeader("Authorization") String token) {
        Long currentUid = JwtUtil.parseUserId(token);
        if (currentUid == null) return Result.fail("Not logged in");

        item.setUserId(currentUid);

        // 检查是否已存在
        QueryWrapper<CartItem> q = new QueryWrapper<>();
        q.eq("user_id", currentUid).eq("prod_id", item.getProdId());
        CartItem exist = cartItemMapper.selectOne(q);

        if (exist != null) {
            exist.setQty(exist.getQty() + item.getQty());
            cartItemMapper.updateById(exist);
        } else {
            cartItemMapper.insert(item);
        }
        return Result.ok("Added to cart");
    }

    // 删除购物车项
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        cartItemMapper.deleteById(id);
        return Result.ok("Deleted");
    }
}