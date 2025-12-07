package com.trader.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

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
                                "/api/prod/{id}", // 这里的 id 可能是数字
                                "/api/prod/listByDistance",
                                "/uploads/**",
                                "/ws/**"
                        ).permitAll()
                        // 🔥 新增：只有拥有 ADMIN 角色才能访问管理员接口
                        // 注意：数据库中存储的角色通常是 "ADMIN"，Spring Security 默认可能需要 "ROLE_ADMIN"
                        // 如果 UserCtrl 存入的是 "ADMIN"，这里需确保 JwtFilter 构建 Authentication 时没有加前缀，或者在这里匹配
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN") // 或者使用 .hasRole("ADMIN") 取决于 GrantedAuthority 的构建方式
                        .anyRequest().authenticated()
                )
                // 添加 JWT 過濾器
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}