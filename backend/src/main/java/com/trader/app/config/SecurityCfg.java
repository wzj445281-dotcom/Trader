package com.trader.app.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

@Configuration
public class SecurityCfg {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeHttpRequests()
            .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/prod/list", "/api/prod/*", "/uploads/**").permitAll()
            .requestMatchers("/api/prod/publish", "/api/prod/fav", "/api/prod/favs/*").authenticated()
            .anyRequest().permitAll()
            .and()
            .addFilterBefore(new JwtFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
