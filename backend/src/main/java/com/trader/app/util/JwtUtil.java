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
        return createToken(userId, null, null);
    }

    // 🔥 修改：增加 role 参数
    public static String createToken(Long userId, String username, String role) {
        long now = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXP))
                .signWith(key);

        if (username != null) builder.claim("username", username);
        // 🔥 存入角色
        if (role != null) builder.claim("role", role);

        return builder.compact();
    }

    public static Long parseUserId(String token) {
        if (token == null) return null;
        try {
            return Long.valueOf(getClaims(token).getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    // 🔥 新增：解析角色
    public static String parseUserRole(String token) {
        if (token == null) return null;
        try {
            return (String) getClaims(token).get("role");
        } catch (Exception e) {
            return null;
        }
    }

    private static Claims getClaims(String token) {
        if (token.startsWith("Bearer ")) token = token.substring(7);
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}