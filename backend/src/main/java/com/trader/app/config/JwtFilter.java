package com.trader.app.config;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import com.trader.app.util.JwtUtil;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JwtFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String auth = req.getHeader("Authorization");
        Long uid = JwtUtil.parseUserId(auth);

        if (uid != null) {
            // 🔥 新增：解析角色
            String role = JwtUtil.parseUserRole(auth);

            // 构建权限列表
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (role != null) {
                // Spring Security 通常对 Role 不强制加 ROLE_ 前缀，取决于配置
                // 这里我们直接使用 "ADMIN" 字符串，与 SecurityCfg 中的 .hasAuthority("ADMIN") 对应
                authorities.add(new SimpleGrantedAuthority(role));
            }

            // 🔥 修改：将 authorities 传入，而不是 Collections.emptyList()
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(uid, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        chain.doFilter(req, res);
    }
}