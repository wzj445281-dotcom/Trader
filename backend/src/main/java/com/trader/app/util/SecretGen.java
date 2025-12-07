package com.trader.app.util;
import java.util.Base64;
import java.security.SecureRandom;
public class SecretGen {
    public static void main(String[] args) {
        byte[] b = new byte[64];
        new SecureRandom().nextBytes(b);
        System.out.println(Base64.getEncoder().encodeToString(b));
    }
}
