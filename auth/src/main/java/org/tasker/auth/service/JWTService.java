package org.tasker.auth.service;

public interface JWTService {
    String generateToken(String id);

    String verifyToken(String token);
}
