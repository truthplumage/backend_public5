package com.grepp.backend5.filter;

import com.grepp.backend5.member.util.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    public final JwtProvider jwtProvider;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        if(request.getContextPath().startsWith("/api/member/login")
                && request.getMethod().equals(HttpMethod.POST.name())){

        }else{
            String bearerToken = request.getHeader("Authorization");
            String token = bearerToken.substring("bearer ".length());
            Jws<Claims> claimsJws = jwtProvider.validateToken(token);
            String id = claimsJws.getPayload().getSubject();
            //TODO: id를 가지고 role 테이블에서 권한 체크후 API 호출 되도록 수정필요.
        }
        filterChain.doFilter(request, response);
    }
}
