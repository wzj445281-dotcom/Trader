package com.trader.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // 导入 HttpMethod
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
                // 1. CORS 配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 2. 关闭 CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 3. 权限配置
                .authorizeHttpRequests(auth -> auth
                        // === 0. 🔥 全局放行所有 OPTIONS 请求 (解决跨域 403) ===
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // === 1. 静态资源与 WebSocket ===
                        .requestMatchers("/uploads/**", "/ws/**").permitAll()

                        // === 2. 认证接口 (登录/注册) ===
                        .requestMatchers("/api/auth/**").permitAll()

                        // === 3. 🔥 显式放行评论接口 (不限 GET/POST，防止误杀) ===
                        // 把它放在通配符前面，确保优先级
                        .requestMatchers("/api/prod/comments/**").permitAll()

                        // === 4. 放行其他商品信息的读取 (列表、详情等) ===
                        .requestMatchers(HttpMethod.GET, "/api/prod/**").permitAll()

                        // === 管理员接口 ===
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // === 其他所有请求都需要登录 ===
                        .anyRequest().authenticated()
                )
                // 4. 添加 JWT 过滤器
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}