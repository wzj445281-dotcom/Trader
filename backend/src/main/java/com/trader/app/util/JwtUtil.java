package com.trader.app.util;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtUtil {
    // 30 days expiry for demo
    private static final long EXP = Long.parseLong(System.getProperty("jwt.access-exp-ms", System.getenv().getOrDefault("JWT_ACCESS_EXP_MS","3600000")));
    private static final Key key;
    static {
        String s = System.getProperty("jwt.secret");
        if (s==null || s.isBlank()) s = System.getenv("JWT_SECRET");
        if (s==null || s.isBlank()) s = "ReplaceThisWithAStrongRandomKeyOrOverride";
        byte[] bytes = s.getBytes();
        if (bytes.length < 32) { // ensure 256-bit
            byte[] ext = new byte[32];
            System.arraycopy(bytes, 0, ext, 0, Math.min(bytes.length, 32));
            bytes = ext;
        }
        key = Keys.hmacShaKeyFor(bytes);
    }

    public static String genToken(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXP))
                .signWith(key)
                .compact();
    }

    public static Long parseUserId(String token) {
        if (token == null) return null;
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            Jws<Claims> j = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            String sub = j.getBody().getSubject();
            return Long.valueOf(sub);
        } catch (Exception e) {
            return null;
        }
    }
}
