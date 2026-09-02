package com.example.portfolio_api.controller;

import com.example.portfolio_api.dto.auth.SignupRequest;
import com.example.portfolio_api.dto.user.UserResponse;
import com.example.portfolio_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest request) {
        authService.signup(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 토큰을 이용한 로그인
    @PostMapping("/login")
    public String login(@RequestBody SignupRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    // 마이 페이지
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }
}
