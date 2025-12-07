package com.trader.app.entity;
import lombok.Data;
@Data
public class ChatMessage {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String message;
    private Long createdAt;
}
