package com.trader.app.entity;
import lombok.Data;
@Data
public class RefreshToken {
    private Long id;
    private Long userId;
    private String token;
    private Long expiresAt;
}
