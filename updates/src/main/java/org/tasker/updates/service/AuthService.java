package org.tasker.updates.service;

import org.tasker.updates.models.dto.RegisterRequest;

public interface AuthService {
    void registerNewUser(RegisterRequest registerRequest);
}
