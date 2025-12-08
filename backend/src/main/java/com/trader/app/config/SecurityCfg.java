package com.trader.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

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
                        // === 0. 全局放行所有 OPTIONS 请求 (解决跨域 403) ===
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // === 1. 静态资源与 WebSocket ===
                        .requestMatchers("/uploads/**", "/ws/**").permitAll()

                        // === 2. 认证接口 (登录/注册/刷新Token) ===
                        .requestMatchers("/api/auth/**").permitAll()

                        // 🔥 核心修复点：明确放行获取评论的 GET 请求
                        .requestMatchers(HttpMethod.GET, "/api/prod/comments/**").permitAll()

                        // === 4. 放行其他商品信息的读取 (列表、详情等) ===
                        .requestMatchers(HttpMethod.GET, "/api/prod/**").permitAll()

                        // === 管理员接口 ===
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // === 其他所有请求都需要登录 (包括 POST /api/prod/comment) ===
                        .anyRequest().authenticated()
                )
                // 4. 添加 JWT 过滤器
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class)

                // 5. 核心：配置异常处理，确保未认证返回 401
                .exceptionHandling(e -> e
                        // 当用户未登录/Token无效访问受保护接口时 -> 返回 401 (前端触发刷新或跳转登录)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                            response.getWriter().write("{\"code\":-1, \"msg\":\"Unauthorized: Please login or token expired\"}");
                        })
                        // 当用户已登录但权限不足时 -> 返回 403
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                            response.getWriter().write("{\"code\":-1, \"msg\":\"Forbidden: Access Denied\"}");
                        })
                );

        return http.build();
    }
}