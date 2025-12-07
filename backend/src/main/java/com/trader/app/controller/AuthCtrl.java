package com.trader.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trader.app.entity.RefreshToken;
import com.trader.app.entity.User;
import com.trader.app.mapper.RefreshTokenMapper;
import com.trader.app.mapper.UserMapper;
import com.trader.app.util.JwtUtil;
import com.trader.app.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthCtrl {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody User u) {
        QueryWrapper<User> q = new QueryWrapper<>();
        q.eq("username", u.getUsername());
        if (userMapper.selectOne(q) != null) return Result.fail("username exists");

        // hash password
        String raw = u.getPassword();
        u.setPassword(encoder.encode(raw));
        // 默认新用户没有权限，或者你可以设置为 "USER"
        if (u.getRole() == null) u.setRole("USER");

        userMapper.insert(u);
        u.setPassword(null);

        Map<String, Object> resp = new HashMap<>();
        resp.put("user", u);

        // 🔥 修改：传入 role
        String token = JwtUtil.createToken(u.getId(), u.getUsername(), u.getRole());
        resp.put("token", token);

        return Result.ok(resp);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User r) {
        QueryWrapper<User> q = new QueryWrapper<>();
        q.eq("username", r.getUsername());
        User u = userMapper.selectOne(q);

        if (u == null) return Result.fail("invalid cred");
        if (!encoder.matches(r.getPassword(), u.getPassword())) return Result.fail("invalid cred");

        u.setPassword(null);

        // 🔥 修改：传入 role
        String accessToken = JwtUtil.createToken(u.getId(), u.getUsername(), u.getRole());

        // 2. 生成 Refresh Token
        String refreshTokenStr = UUID.randomUUID().toString();
        long refreshExpMs = Long.parseLong(System.getProperty("jwt.refresh-exp-ms",
                System.getenv().getOrDefault("JWT_REFRESH_EXP_MS", "2592000000")));
        long expiresAt = System.currentTimeMillis() + refreshExpMs;

        RefreshToken rt = new RefreshToken();
        rt.setUserId(u.getId());
        rt.setToken(refreshTokenStr);
        rt.setExpiresAt(expiresAt);
        refreshTokenMapper.insert(rt);

        Map<String, Object> resp = new HashMap<>();
        resp.put("user", u);
        resp.put("token", accessToken);
        resp.put("refresh", refreshTokenStr);

        return Result.ok(resp);
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String ref = body.get("refresh");
        if (ref == null) return Result.fail("no refresh");

        RefreshToken t = refreshTokenMapper.selectOne(new QueryWrapper<RefreshToken>().eq("token", ref));
        if (t == null) return Result.fail("invalid refresh");

        if (t.getExpiresAt() < System.currentTimeMillis()) {
            refreshTokenMapper.deleteById(t.getId());
            return Result.fail("refresh expired");
        }

        // 刷新 Token 时，为了简单起见，暂时不查 Role，或者你应该再查一次 User 表获取最新 Role
        // 这里演示重新查库获取 Role
        User u = userMapper.selectById(t.getUserId());
        String role = (u != null) ? u.getRole() : null;
        String username = (u != null) ? u.getUsername() : null;

        // 🔥 修改：生成新 Token 带上 Role
        String newAccess = JwtUtil.createToken(t.getUserId(), username, role);
        return Result.ok(Map.of("token", newAccess));
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestBody Map<String, String> body) {
        String ref = body.get("refresh");
        if (ref == null) return Result.fail("no refresh");

        refreshTokenMapper.delete(new QueryWrapper<RefreshToken>().eq("token", ref));
        return Result.ok("logged out");
    }
}