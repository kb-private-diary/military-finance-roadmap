package org.scoula.security.filter;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.security.util.JwtProcessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtProcessor jwtProcessor;
    private final UserDetailsService userDetailsService;

    public Authentication  getAuthentication(String token) {
        String username = jwtProcessor.getUsername(token); //유효성검증 --> 로그인한 username
        UserDetails princiapl = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(princiapl, null, princiapl.getAuthorities());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)){
            String token = bearerToken.substring(BEARER_PREFIX.length()); //7인덱스이후의 문자열 추출

            try {
                Authentication authentication = getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException e) {
                log.debug("유효하지 않은 토큰이라 인증 없이 진행합니다: {}", e.getMessage());
            }
        }
        super.doFilter(request, response, filterChain);//다음 필터 연결
    }
}
