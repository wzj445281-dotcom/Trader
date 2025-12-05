package com.trader.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trader.app.entity.RefreshToken;
import com.trader.app.entity.User;
import com.trader.app.mapper.RefreshTokenMapper;
import com.trader.app.mapper.UserMapper;
import com.trader.app.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired UserMapper userMapper;
    @Autowired RefreshTokenMapper refreshTokenMapper;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${jwt.refresh-exp-ms:2592000000}")
    private long refreshExpMs;

    public Map<String, Object> register(User u) {
        QueryWrapper<User> q = new QueryWrapper<>();
        q.eq("username", u.getUsername());
        if (userMapper.selectOne(q) != null) throw new IllegalArgumentException("Username already exists");

        u.setPassword(encoder.encode(u.getPassword()));
        userMapper.insert(u);
        return generateLoginResponse(u);
    }

    public Map<String, Object> login(String username, String rawPassword) {
        QueryWrapper<User> q = new QueryWrapper<>();
        q.eq("username", username);
        User u = userMapper.selectOne(q);

        if (u == null || !encoder.matches(rawPassword, u.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return generateLoginResponse(u);
    }

    public Map<String, String> refreshToken(String refreshToken) {
        if (refreshToken == null) throw new IllegalArgumentException("No refresh token provided");

        RefreshToken t = refreshTokenMapper.selectOne(new QueryWrapper<RefreshToken>().eq("token", refreshToken));
        if (t == null) throw new IllegalArgumentException("Invalid refresh token");

        if (t.getExpiresAt() < System.currentTimeMillis()) {
            refreshTokenMapper.deleteById(t.getId());
            throw new IllegalArgumentException("Refresh token expired");
        }

        String newAccess = JwtUtil.genToken(t.getUserId());
        return Map.of("token", newAccess);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenMapper.delete(new QueryWrapper<RefreshToken>().eq("token", refreshToken));
        }
    }

    private Map<String, Object> generateLoginResponse(User u) {
        String access = JwtUtil.genToken(u.getId());
        String refresh = UUID.randomUUID().toString();

        RefreshToken rt = new RefreshToken();
        rt.setUserId(u.getId());
        rt.setToken(refresh);
        rt.setExpiresAt(System.currentTimeMillis() + refreshExpMs);
        refreshTokenMapper.insert(rt);

        u.setPassword(null); // Ensure password is removed
        Map<String, Object> resp = new HashMap<>();
        resp.put("user", u);
        resp.put("token", access);
        resp.put("refresh", refresh);
        return resp;
    }
}