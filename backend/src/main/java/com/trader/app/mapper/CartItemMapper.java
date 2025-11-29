package com.trader.app.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trader.app.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {}
