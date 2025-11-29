package com.trader.app.controller;

import com.trader.app.entity.Report;
import com.trader.app.entity.User;
import com.trader.app.mapper.ReportMapper;
import com.trader.app.mapper.UserMapper;
import com.trader.app.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/report")
public class AdminReportCtrl {

    @Autowired
    ReportMapper reportMapper;

    @Autowired
    UserMapper userMapper;

    // 获取所有举报记录
    @GetMapping("/list")
    public Result<List<Report>> list() {
        List<Report> list = reportMapper.selectList(null);
        return Result.ok(list);
    }

    // 查询被举报的用户信息
    @GetMapping("/user/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        return Result.ok(u);
    }

    // 删除一条举报记录
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        reportMapper.deleteById(id);
        return Result.ok();
    }
}
