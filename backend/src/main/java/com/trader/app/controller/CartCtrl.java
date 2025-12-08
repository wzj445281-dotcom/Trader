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

    // 🔥 优化：不再依赖 URL 中的 userId，完全从 JWT 中获取用户 ID。
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getCart(@RequestHeader("Authorization") String token) {
        // 安全校验：直接从 JWT 获取用户 ID，如果获取失败，则未认证。
        Long currentUid = JwtUtil.parseUserId(token);
        if (currentUid == null) {
            // 如果 JWT 解析失败，可能是 token 无效或过期，返回认证失败
            return Result.fail("Authentication required to access cart");
        }

        QueryWrapper<CartItem> q = new QueryWrapper<>();
        q.eq("user_id", currentUid); // 使用 JWT 中的用户 ID
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
            item.setQty(item.getQty() == null ? 1 : item.getQty()); // 确保 qty 不为空
            cartItemMapper.insert(item);
        }
        return Result.ok("Added to cart");
    }
    // 删除购物车项
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long currentUid = JwtUtil.parseUserId(token);
        if (currentUid == null) return Result.fail("Not logged in");

        // 校验权限：确保删除的是自己的购物车项
        QueryWrapper<CartItem> q = new QueryWrapper<>();
        q.eq("id", id).eq("user_id", currentUid);
        if (cartItemMapper.delete(q) == 0) {
            return Result.fail("Cart item not found or unauthorized deletion");
        }
        return Result.ok("Deleted");
    }
}