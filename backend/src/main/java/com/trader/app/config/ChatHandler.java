package com.trader.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trader.app.entity.ChatMessage;
import com.trader.app.mapper.ChatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {

    // 用於存儲在線用戶的 Session: Map<UserId, Session>
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 從 URL 參數獲取用戶 ID，例如 ws://.../ws/chat?uid=123
        String q = session.getUri().getQuery();
        Long uid = null;
        if (q != null && q.contains("uid=")) {
            try {
                String[] parts = q.split("uid=");
                if (parts.length > 1) {
                    String s = parts[1].split("&")[0];
                    uid = Long.valueOf(s);
                }
            } catch(Exception e){
                // 解析失敗忽略
            }
        }

        if (uid != null) {
            userSessions.put(uid, session);
            System.out.println("User connected: " + uid);
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        // 預期 JSON 格式: { "to": 2, "message": "hello", "from": 1 }
        // 注意：為了安全，實際 "from" 應該從 session 綁定的 uid 獲取，這裡暫時沿用 payload
        Map<String, Object> map = mapper.readValue(payload, Map.class);

        Long to = map.get("to") == null ? null : Long.valueOf(String.valueOf(map.get("to")));
        // 這裡嘗試從 payload 獲取 from，但建議改進為從 session 查找
        Long from = null;

        // 簡單反向查找 uid (實際項目可以將 uid 存入 session attributes)
        for (Map.Entry<Long, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                from = entry.getKey();
                break;
            }
        }

        if (from == null && map.get("from") != null) {
            from = Long.valueOf(String.valueOf(map.get("from")));
        }

        String msgContent = map.get("message") == null ? "" : String.valueOf(map.get("message"));

        if (from != null && to != null) {
            // 持久化消息
            try {
                ChatMessage cm = new ChatMessage();
                cm.setFromUserId(from);
                cm.setToUserId(to);
                cm.setMessage(msgContent);
                cm.setCreatedAt(System.currentTimeMillis());
                chatMessageMapper.insert(cm);
            } catch(Exception e){
                e.printStackTrace();
            }

            // 發送給接收者
            if (userSessions.containsKey(to)) {
                WebSocketSession s = userSessions.get(to);
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(mapper.writeValueAsString(
                            Map.of("from", from, "message", msgContent, "ts", System.currentTimeMillis())
                    )));
                }
            }

            // 可選：回顯給發送者確認發送成功 (前端通常會自己顯示，不需要後端回顯，視需求而定)
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        userSessions.values().remove(session);
        System.out.println("Connection closed");
    }
}