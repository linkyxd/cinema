package com.example.cinema.repository;

import com.example.cinema.model.SessionStatus;
import com.example.cinema.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByRefreshTokenIdAndStatus(String refreshTokenId, SessionStatus status);
}
