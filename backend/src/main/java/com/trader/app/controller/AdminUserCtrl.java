package com.trader.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trader.app.entity.User;
import com.trader.app.mapper.UserMapper;
import com.trader.app.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserCtrl {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 获取所有用户列表
    @GetMapping("")
    public Result<List<User>> list() {
        // 出于安全，不返回密码
        List<User> list = userMapper.selectList(null);
        list.forEach(u -> u.setPassword(null));
        return Result.ok(list);
    }

    // 管理员重置用户密码 (默认重置为 123456)
    @PostMapping("/{id}/reset-pwd")
    public Result<String> resetPwd(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.fail("用户不存在");

        u.setPassword(encoder.encode("123456"));
        userMapper.updateById(u);
        return Result.ok("密码已重置为 123456");
    }

    // 删除/禁用用户
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        // 实际项目中建议做软删除 (status=DELETED)
        userMapper.deleteById(id);
        return Result.ok("用户已删除");
    }
}