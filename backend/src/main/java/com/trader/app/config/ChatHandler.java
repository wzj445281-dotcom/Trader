package com.trader.app.config;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trader.app.entity.ChatMessage;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class ChatHandler extends TextWebSocketHandler {
    private static Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // expect a query param ?uid=123
        String q = session.getUri().getQuery();
        Long uid = null;
        if (q != null && q.contains("uid=")) {
            try {
                String[] parts = q.split("uid=");
                String s = parts[1].split("&")[0];
                uid = Long.valueOf(s);
            } catch(Exception e){}
        }
        if (uid != null) userSessions.put(uid, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        // expect JSON: { "to": 2, "message": "hello" }
        Map map = mapper.readValue(payload, Map.class);
        Long to = map.get("to") == null ? null : Long.valueOf(String.valueOf(map.get("to")));
        Long from = map.get("from") == null ? null : Long.valueOf(String.valueOf(map.get("from")));
        String msg = map.get("message") == null ? "" : String.valueOf(map.get("message"));

        // persist message via ChatMessageMapper from spring context
        try {
            WebApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
            com.trader.app.mapper.ChatMessageMapper mm = ctx.getBean(com.trader.app.mapper.ChatMessageMapper.class);
            ChatMessage cm = new ChatMessage();
            cm.setFromUserId(from);
            cm.setToUserId(to);
            cm.setMessage(msg);
            cm.setCreatedAt(System.currentTimeMillis());
            mm.insert(cm);
        } catch(Exception e){ /* ignore persistence errors */ }

        // send to recipient if connected
        if (to != null && userSessions.containsKey(to)) {
            WebSocketSession s = userSessions.get(to);
            if (s.isOpen()) s.sendMessage(new TextMessage(mapper.writeValueAsString(Map.of("from", from, "message", msg, "ts", System.currentTimeMillis()))));
        }
        // echo back to sender as acknowledgement
        if (from != null && userSessions.containsKey(from)) {
            WebSocketSession s = userSessions.get(from);
            if (s.isOpen()) s.sendMessage(new TextMessage(mapper.writeValueAsString(Map.of("to", to, "message", msg, "ts", System.currentTimeMillis()))));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        userSessions.values().remove(session);
    }
}
