package com.example.portfolio_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 보통의 브라우저는 보안 정책 때문에 주소의 도메인(IP), 포트, 프로토콜 중 하나라도 다르면 다른 출처(origin)으로
//      간주하고, 요청 결과를 기본적으로 차단한다.
// 그러나 CORS 설정을 사용한다면 프론트엔드(localhost:5173)에서 오는 요청은 안전하니 허용해 달라고
//      브라우저에 명시해 줄 수 있다.

// WebMvcConfigurer: Spring MVC의 기본 웹 설정을 가로채 커스텀 설정을 추가할 수 있게 해주는 인터페이스
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // CORS 관련 규칙을 정의하는 전용 메서드 오버라이드
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 서버의 엔드포인트 중 /api/로 시작하는 모든 URL 경로에 대해 아래 규칙을 적용
        //      allowedOrigins: 허용 출처 지정
        //      allowedMethods: 허용할 HTTP 메서드 명시
        //          OPTIONS: 사전 요청(Preflight) 메서드. 전송 전 미리 허용 하는지 확인한다.
        //      allowedHeaders: 클라이언트가 요청 시 전송할 헤더 (Content-Type, Authorization 등) 허용
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
