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

    // 用于存储在线用户的 Session: Map<UserId, Session>
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
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
                // 解析失败忽略
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
        Map<String, Object> map = mapper.readValue(payload, Map.class);

        Long to = map.get("to") == null ? null : Long.valueOf(String.valueOf(map.get("to")));

        // 修复：强制从 Session 反查发送者 ID，不信任前端传来的 "from"
        Long from = null;
        for (Map.Entry<Long, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                from = entry.getKey();
                break;
            }
        }

        if (from == null) {
            // 未登录或连接异常，不处理
            return;
        }

        String msgContent = map.get("message") == null ? "" : String.valueOf(map.get("message"));

        if (to != null) {
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

            // 发送给接收者
            if (userSessions.containsKey(to)) {
                WebSocketSession s = userSessions.get(to);
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(mapper.writeValueAsString(
                            Map.of("from", from, "message", msgContent, "ts", System.currentTimeMillis())
                    )));
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        userSessions.values().remove(session);
        System.out.println("Connection closed");
    }
}