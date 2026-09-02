package com.example.portfolio_api.config;

import com.example.portfolio_api.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Spring Security를 사용해 REST API에 최적화된 Stateless 보안 규칙과 URL 접근 권한 설정

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // .csrf(csrf -> csrf.disable()):
                //      보통 브라우저 기반은 쿠키 세션 사용 시 CSRF 토큰이 필수적이다.
                //      그러나 REST API는 세션을 저장하지 않는 stateless 방식이므로
                //      토큰(주로 JWT) 기반 인증을 사용할 때는 이를 비활성화 한다.
                // .cors(Customizer.withDefaults()):
                //      WebConfig.java에 지정한 CORS 규칙을 Spring Security 필터 체인에도 적용한다.
                //      이 구문이 있어야 OPTIONS와 같은 Preflight 등이 거부되지 않는다.
                // .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)):
                //      세션 미사용(stateless)를 명시하는 구문
                // .authorizeHttpRequests():
                //      URL 경로 별 인가(Authorization) 정책을 지정한다.
                //          /error:
                //              기본적으로 예외가 발생하면 Spring은 내부적으로 /error로 넘긴다.
                //              이것이 없으면 모든 에러가 403이 되므로 원인 추적이 어렵게 된다.
                //      permitAll(): 인증 없이 누구나 접근 가능하도록 열어둔다.
                //      anyRequest().authenticatied(): 명시된 경로를 제오이한 나머지 요청은 유효한 인증이 필요

                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // 웬만한 경로는 열려 있음
                        .requestMatchers(
                                "/error",
                                "/api/ping",
                                "/api/auth/login",
                                "/api/auth/signup")
                        .permitAll()

                        // GET /api/posts는 열림, POST /api/posts는 닫힘
                        .requestMatchers(HttpMethod.GET, "/api/posts/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        (req, res, ex) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                ))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 비밀번호 암호화 빈
    //      회원가입 시 사용자의 비밀번호를 BCrypt으로 암호화하도록 설정 (해시값)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
