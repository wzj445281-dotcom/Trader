package com.trader.app.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class JwtUtilTest {
    @Test
    public void testToken() {
        String token = JwtUtil.genToken(123L);
        assertNotNull(token);
        Long id = JwtUtil.parseUserId(token);
        assertEquals(123L, id);
    }
}
