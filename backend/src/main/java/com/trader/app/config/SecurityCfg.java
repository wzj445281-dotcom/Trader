package com.trader.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityCfg {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 🔥 关键修复：显式开启 CORS 并使用自定义源
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 关闭 CSRF (REST API 不需要)
                .csrf(AbstractHttpConfigurer::disable)
                // 配置路由权限
                .authorizeHttpRequests(auth -> auth
                        // 允许匿名访问的接口
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/prod/list",
                                "/api/prod/view/**",
                                "/api/prod/recommend/**",
                                "/api/prod/comments/**",
                                "/api/prod/{id}",
                                "/api/prod/listByDistance",
                                "/uploads/**",
                                "/ws/**"
                        ).permitAll()
                        // 管理员接口
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        // 购物车、下单等其他接口都需要登录
                        .anyRequest().authenticated()
                )
                // 添加 JWT 过滤器
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}