package org.tasker.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.auth.service.JWTService;

import java.security.Key;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
public class JWTServiceImpl implements JWTService {

    private final JWTProperties jwtProperties;
    private final Key key;

    public JWTServiceImpl(JWTProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    @Override
    public String generateToken(String id) {
        log.debug("(generateToken) Generating token for aggregate ID: {}", id);

        Duration expirationDuration = jwtProperties.getExpiration();
        Instant expirationTime = Instant.now().plus(expirationDuration);

        return Jwts.builder()
                .setId(id)
                .signWith(key)
                .setExpiration(Date.from(expirationTime))
                .compact();
    }

    @Override
    public String verifyToken(String token) {
        log.debug("(verifyToken) Verifying token: {}", token);
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            log.debug("(verifyToken) Token verified: {}", claimsJws.getBody().getId());
            return claimsJws.getBody().getId();
        } catch (Exception e) {
            log.debug("(verifyToken) Invalid token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }
}
