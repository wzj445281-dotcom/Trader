package com.trader.app.controller;
import com.trader.app.entity.User;
import com.trader.app.mapper.UserMapper;
import com.trader.app.util.Result;
import com.trader.app.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthCtrl {
    @Autowired
    UserMapper userMapper;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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
        u.setPassword(null);
        String token = JwtUtil.createToken(u.getId(), u.getUsername());
        Map<String, Object> resp = new HashMap<>();
        resp.put("user", u);
        resp.put("token", token);
        return Result.ok(resp);
    }


    @Autowired
    private com.trader.app.mapper.RefreshTokenMapper refreshTokenMapper;

    @PostMapping("/login")
    public Result<java.util.Map<String, Object>> loginWithToken(@RequestBody com.trader.app.entity.User r) {
        com.trader.app.entity.User u = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.trader.app.entity.User>().eq("username", r.getUsername()));
        if (u == null) return Result.fail("invalid cred");
        if (!org.springframework.security.crypto.bcrypt.BCrypt.checkpw(r.getPassword(), u.getPassword()))
            return Result.fail("invalid cred");
        String access = JwtUtil.genToken(u.getId());
        // create refresh token
        String ref = java.util.UUID.randomUUID().toString();
        long expires = System.currentTimeMillis() + Long.parseLong(System.getProperty("jwt.refresh-exp-ms", System.getenv().getOrDefault("JWT_REFRESH_EXP_MS", "2592000000")));
        com.trader.app.entity.RefreshToken rt = new com.trader.app.entity.RefreshToken();
        rt.setUserId(u.getId());
        rt.setToken(ref);
        rt.setExpiresAt(expires);
        refreshTokenMapper.insert(rt);
        u.setPassword(null);
        return Result.ok(java.util.Map.of("token", access, "refresh", ref, "user", u));
    }

    @PostMapping("/refresh")
    public Result<java.util.Map<String, String>> refresh(@RequestBody java.util.Map<String, String> body) {
        String ref = body.get("refresh");
        if (ref == null) return Result.fail("no refresh");
        com.trader.app.entity.RefreshToken t = refreshTokenMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.trader.app.entity.RefreshToken>().eq("token", ref));
        if (t == null) return Result.fail("invalid refresh");
        if (t.getExpiresAt() < System.currentTimeMillis()) {
            refreshTokenMapper.deleteById(t.getId());
            return Result.fail("refresh expired");
        }
        String newAccess = JwtUtil.genToken(t.getUserId());
        return Result.ok(java.util.Map.of("token", newAccess));
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestBody java.util.Map<String, String> body) {
        String ref = body.get("refresh");
        if (ref == null) return Result.fail("no refresh");
        refreshTokenMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.trader.app.entity.RefreshToken>().eq("token", ref));
        return Result.ok("logged out");
    }
}
