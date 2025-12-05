package com.trader.app.controller;

<<<<<<< HEAD
import com.trader.app.entity.User;
import com.trader.app.service.AuthService;
import com.trader.app.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
=======
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
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthCtrl {

    @Autowired
<<<<<<< HEAD
    private AuthService authService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody User u) {
        return Result.ok(authService.register(u));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User u) {
        return Result.ok(authService.login(u.getUsername(), u.getPassword()));
=======
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
        userMapper.insert(u);
        u.setPassword(null);

        Map<String, Object> resp = new HashMap<>();
        resp.put("user", u);
        // 使用我們剛修復的 createToken 方法，帶入 username
        String token = JwtUtil.createToken(u.getId(), u.getUsername());
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

        // 清除密碼，避免返回給前端
        u.setPassword(null);

        // 1. 生成 Access Token (包含 username)
        String accessToken = JwtUtil.createToken(u.getId(), u.getUsername());

        // 2. 生成 Refresh Token
        String refreshTokenStr = UUID.randomUUID().toString();
        // 讀取配置的過期時間，默認為 30 天
        long refreshExpMs = Long.parseLong(System.getProperty("jwt.refresh-exp-ms",
                System.getenv().getOrDefault("JWT_REFRESH_EXP_MS", "2592000000")));
        long expiresAt = System.currentTimeMillis() + refreshExpMs;

        // 保存 Refresh Token 到資料庫
        RefreshToken rt = new RefreshToken();
        rt.setUserId(u.getId());
        rt.setToken(refreshTokenStr);
        rt.setExpiresAt(expiresAt);
        refreshTokenMapper.insert(rt);

        // 3. 構建返回結果
        Map<String, Object> resp = new HashMap<>();
        resp.put("user", u);
        resp.put("token", accessToken);
        resp.put("refresh", refreshTokenStr);

        return Result.ok(resp);
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
<<<<<<< HEAD
        return Result.ok(authService.refreshToken(body.get("refresh")));
=======
        String ref = body.get("refresh");
        if (ref == null) return Result.fail("no refresh");

        RefreshToken t = refreshTokenMapper.selectOne(new QueryWrapper<RefreshToken>().eq("token", ref));
        if (t == null) return Result.fail("invalid refresh");

        if (t.getExpiresAt() < System.currentTimeMillis()) {
            refreshTokenMapper.deleteById(t.getId());
            return Result.fail("refresh expired");
        }

        // 生成新的 Access Token (這裡暫時不查用戶名，使用 genToken 即可)
        String newAccess = JwtUtil.genToken(t.getUserId());
        return Result.ok(Map.of("token", newAccess));
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestBody Map<String, String> body) {
<<<<<<< HEAD
        authService.logout(body.get("refresh"));
        return Result.ok("Logged out");
=======
        String ref = body.get("refresh");
        if (ref == null) return Result.fail("no refresh");

        refreshTokenMapper.delete(new QueryWrapper<RefreshToken>().eq("token", ref));
        return Result.ok("logged out");
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
    }
}