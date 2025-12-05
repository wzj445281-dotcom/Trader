package com.trader.app.controller;

import com.trader.app.entity.User;
import com.trader.app.service.AuthService;
import com.trader.app.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthCtrl {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody User u) {
        return Result.ok(authService.register(u));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User u) {
        return Result.ok(authService.login(u.getUsername(), u.getPassword()));
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        return Result.ok(authService.refreshToken(body.get("refresh")));
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestBody Map<String, String> body) {
        authService.logout(body.get("refresh"));
        return Result.ok("Logged out");
    }
}