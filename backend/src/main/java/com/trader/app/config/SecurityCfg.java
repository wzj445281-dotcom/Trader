package com.trader.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityCfg {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 關閉 CSRF (因為使用 JWT)
                .csrf(AbstractHttpConfigurer::disable)
                // 配置路由權限
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/prod/list",
                                "/api/prod/view/**",
                                "/api/prod/recommend/**",
                                "/api/prod/comments/**",
                                "/api/prod/{id}",
                                "/api/prod/listByDistance",
                                "/uploads/**",
                                "/ws/**" // 允許 WebSocket 連接
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // 添加 JWT 過濾器
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}