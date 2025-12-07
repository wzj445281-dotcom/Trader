package com.trader.app.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trader.app.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {}
