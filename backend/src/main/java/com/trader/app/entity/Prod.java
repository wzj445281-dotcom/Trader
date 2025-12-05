package com.trader.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Prod {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String title;
    private String descr;
    private Double price;

    // 地理位置信息
    private Double lat;
    private Double lng;

    private Integer viewCount;
    private String images;
    private String category;
    private String status;
    private LocalDateTime createdAt;
}