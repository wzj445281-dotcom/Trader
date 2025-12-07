package java.com.trader.app.controller;
import org.springframework.web.bind.annotation.*;
import com.trader.app.mapper.ChatMessageMapper;
import com.trader.app.entity.ChatMessage;
import com.trader.app.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatCtrl {
    @Autowired
    ChatMessageMapper chatMessageMapper;

    @GetMapping("/history/{userA}/{userB}")
    public Result<List<ChatMessage>> history(@PathVariable Long userA, @PathVariable Long userB){
        List<ChatMessage> hs = chatMessageMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ChatMessage>()
            .eq("from_user_id", userA).eq("to_user_id", userB).or()
            .eq("from_user_id", userB).eq("to_user_id", userA).orderByAsc("created_at")
        );
        return Result.ok(hs);
    }
}
