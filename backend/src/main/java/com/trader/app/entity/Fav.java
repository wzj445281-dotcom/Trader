package com.trader.app.entity;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
@Data
public class Fav {
    @TableId
    private Long id;
    private Long userId;
    private Long prodId;
}
