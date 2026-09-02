package com.example.portfolio_api.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// 클라이언트가 보낸 HTTP 요청 헤더에서 JWT를 꺼내 검증하고, 유효한 사용자인 경우 인증 상태로 등록해준다.
//      서블릿(Survlet): 클라이언트의 HTTP 요청을 받아 처리하고, 그 결과를 다시 돌려주는 자바 기반 웹 컴포넌트

// extends OncePerRequestFilter: Spring Security 또는 서블릿 환경에서 단일 HTTP 요청 당
//      딱 한 번만 실행되도록 보장하는 추상 필터 클래스이다. 즉, 중복 실행을 방지한다.
// header 문자열: 클라이언트 요청의 Authorization 헤더 값을 확인한다.
//      내부에서 header가 비어있지 않고, "Bearer "로 시작하는지 검사한다.
// email 문자열: jwtTokenProvider를 통해 토큰의 Signature와 만료시간을 검증한다.
//      Payload에 담긴 사용자 식별 정보(email)을 추출한다.
// SecurityContextHolder.getContext().setAuthentication():
//      토큰이 유효하다면 UsernamePasswordAuthenticationToken 객체를 생성해, SecurityContext에 저장한다.
//      인자:
//          email: 인증된 사용자 식별자
//          null: 자격 증명. 패스워드는 이미 토큰으로 증명되었으므로 불필요하다.
//          List.of(): 부여할 권한 목록. 현재는 Role 없이 빈 리스트를 전달한다.
//      이 작업이 완수되어야 이후 컨트롤러나 시큐리티 인가 설정에서 "로그인된 사용자"로 인정받는다.
// catch: 토큰이 위조되었거나 유효기간이 지났을 경우 발생하는 예외를 잡는다.
//      에러 응답을 따로 만들지 않고, 인증 객체를 등록하지 않은 채 그냥 통과시킨다.
//      이후에 인증이 필요하라면 401/403 등 에러를, 공개 경로면 통과하도록 자연스럽게 처리한다.
// chain.doFilter(): 필터 검증 작업이 끝났으므로 다음 필터로 요청을 전달한다.

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String email = jwtTokenProvider.getEmail(header.substring(7));
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(email, null, List.of())
                );
            } catch (JwtException e) {
                // 유효하지 않은 토큰: 인증 없이 통과시킨다.
            }
        }

        chain.doFilter(request, response);
    }
}
