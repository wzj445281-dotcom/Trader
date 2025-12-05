package com.trader.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trader.app.entity.User;
import com.trader.app.mapper.UserMapper;
import com.trader.app.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserCtrl {

    @Autowired
    UserMapper userMapper;

    // 更新个人信息
    @PostMapping("/update")
    public Result<User> update(@RequestBody User u) {
        if (u.getId() == null) return Result.fail("User ID missing");

        User exist = userMapper.selectById(u.getId());
        if (exist == null) return Result.fail("User not found");

        // 更新允许修改的字段
        if (u.getEmail() != null) exist.setEmail(u.getEmail());
        if (u.getPhone() != null) exist.setPhone(u.getPhone());
        if (u.getAvatar() != null) exist.setAvatar(u.getAvatar());

        // 如果修改密码 (逻辑比较简单，实际需校验旧密码)
        if (u.getPassword() != null && !u.getPassword().isBlank()) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            exist.setPassword(encoder.encode(u.getPassword()));
        }

        userMapper.updateById(exist);
        exist.setPassword(null); // 不返回密码
        return Result.ok(exist);
    }

    // 获取最新用户信息
    @GetMapping("/{id}")
    public Result<User> getInfo(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u != null) u.setPassword(null);
        return Result.ok(u);
    }
}