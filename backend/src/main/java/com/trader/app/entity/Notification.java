package com.trader.app.entity;
import lombok.Data;
@Data
public class Notification {
    private Long id;
    private Long userId;
    private String title;
    private String body;
    private Boolean read;
    private Long createdAt;
}
