package com.trader.app.entity;
import lombok.Data;
@Data
public class Report {
    private Long id;
    private Long reporterId;
    private Long prodId;
    private String reason;
    private String status; // OPEN, REVIEWED, CLOSED
    private Long createdAt;
}
