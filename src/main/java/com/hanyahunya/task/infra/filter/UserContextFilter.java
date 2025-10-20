package com.hanyahunya.task.infra.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class UserContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");
        String userRoles = request.getHeader("X-User-Role");


        if (userId != null && !userId.isEmpty()) {
            try {
                List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(userRoles);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("UserContextFilter: ユーザー '{}' の SecurityContextHolder が設定されました", userId);

            } catch (Exception e) {
                log.error("UserContextFilter: セキュリティコンテキストにユーザー認証を設定できませんでした", e);
                SecurityContextHolder.clearContext();
            }
        } else {
            log.debug("UserContextFilter: ヘッダーにユーザーIDが存在しません。パス: {}", request.getRequestURI());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}