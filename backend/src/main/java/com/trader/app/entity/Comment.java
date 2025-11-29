package com.trader.app.entity;
import lombok.Data;
@Data
public class Comment {
    private Long id;
    private Long userId;
    private Long prodId;
    private String content;
    private Integer rating;
    private Long createdAt;
}
