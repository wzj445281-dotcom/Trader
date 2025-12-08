package com.trader.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsCfg {

    // 修改：返回 CorsConfigurationSource 供 Security 使用
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // 允许的源：开发环境允许所有，生产环境建议指定
        cfg.setAllowedOriginPatterns(Collections.singletonList("*"));
        // 允许的方法
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的头
        cfg.setAllowedHeaders(Collections.singletonList("*"));
        // 允许凭证 (Cookie/Auth Header)
        cfg.setAllowCredentials(true);
        // 暴露的头 (防止前端拿不到某些 Header)
        cfg.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}