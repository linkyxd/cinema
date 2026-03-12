package com.example.cinema.service;

import com.example.cinema.model.SessionStatus;
import com.example.cinema.model.UserAccount;
import com.example.cinema.model.UserSession;
import com.example.cinema.repository.UserSessionRepository;
import com.example.cinema.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthTokenService {

    private final JwtTokenProvider tokenProvider;
    private final UserSessionRepository sessionRepo;

    public AuthTokenService(JwtTokenProvider tokenProvider, UserSessionRepository sessionRepo) {
        this.tokenProvider = tokenProvider;
        this.sessionRepo = sessionRepo;
    }

    @Transactional
    public Map<String, String> createTokenPair(UserAccount user) {
        String sessionId = UUID.randomUUID().toString();

        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshTokenId(sessionId);
        session.setStatus(SessionStatus.ACTIVE);
        session.setCreatedAt(Instant.now());
        session.setExpiresAt(Instant.now().plusMillis(tokenProvider.getRefreshExpiryMs()));
        sessionRepo.save(session);

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user, sessionId);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    @Transactional
    public Map<String, String> refreshTokens(String refreshTokenValue) {
        Claims claims;
        try {
            claims = tokenProvider.validateRefreshToken(refreshTokenValue);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        String sessionId = claims.get(JwtTokenProvider.CLAIM_SESSION_ID, String.class);
        if (sessionId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        UserSession session = sessionRepo.findByRefreshTokenIdAndStatus(sessionId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session not found or already revoked"));

        session.setStatus(SessionStatus.REVOKED);
        sessionRepo.save(session);

        UserAccount user = session.getUser();
        String newSessionId = UUID.randomUUID().toString();

        UserSession newSession = new UserSession();
        newSession.setUser(user);
        newSession.setRefreshTokenId(newSessionId);
        newSession.setStatus(SessionStatus.ACTIVE);
        newSession.setCreatedAt(Instant.now());
        newSession.setExpiresAt(Instant.now().plusMillis(tokenProvider.getRefreshExpiryMs()));
        sessionRepo.save(newSession);

        String accessToken = tokenProvider.generateAccessToken(user);
        String newRefreshToken = tokenProvider.generateRefreshToken(user, newSessionId);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", newRefreshToken
        );
    }
}
