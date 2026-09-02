package com.example.portfolio_api.service;

import com.example.portfolio_api.domain.User;
import com.example.portfolio_api.dto.user.UserResponse;
import com.example.portfolio_api.jwt.JwtTokenProvider;
import com.example.portfolio_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    public ResponseEntity<Void> signup(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 있는 계정");
        }

        userRepository.save(new User(email, passwordEncoder.encode(password)));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 토큰을 이용한 로그인
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 실패"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 실패");
        }

        return jwtTokenProvider.createToken(user.getEmail());
    }

    // 마이 페이지
    public UserResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        return new UserResponse(user.getId(), user.getEmail());
    }
}
