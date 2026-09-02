package com.example.portfolio_api.repository;

import com.example.portfolio_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
