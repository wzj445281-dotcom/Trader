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

        System.out.println("正在尝试删除 - 用户ID: " + currentUid + ", 传入参数ID: " + id);

        // 尝试 1: 假设前端传的是【购物车记录的主键 ID】
        QueryWrapper<CartItem> q1 = new QueryWrapper<>();
        q1.eq("id", id).eq("user_id", currentUid);
        int rows1 = cartItemMapper.delete(q1);

        if (rows1 > 0) {
            return Result.ok("Deleted successfully (by Cart ID)");
        }

        // 尝试 2: 假设前端传的是【商品 ID (good_id)】
        // 很多时候前端列表渲染的是商品信息，点删除时传的是商品的ID
        QueryWrapper<CartItem> q2 = new QueryWrapper<>();
        q2.eq("good_id", id).eq("user_id", currentUid);
        int rows2 = cartItemMapper.delete(q2);

        if (rows2 > 0) {
            return Result.ok("Deleted successfully (by Good ID)");
        }

        // 如果两次都删不掉，才返回失败
        System.err.println("删除失败 - 未找到对应记录。User: " + currentUid + ", Param ID: " + id);
        return Result.fail("Cart item not found or unauthorized deletion");
    }
}