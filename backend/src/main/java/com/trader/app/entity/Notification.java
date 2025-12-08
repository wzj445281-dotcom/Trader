package com.trader.app.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class Notification {
    private Long id;
    private Long userId;
    private String title;
    private String body;

    // 🔥 核心修复：read 是 MySQL 关键字，必须使用 @TableField("`read`") 加反引号转义
    // 否则执行 insert 语句时会报 SQL 语法错误
    @TableField("`read`")
    private Boolean read;

    private Long createdAt;
}