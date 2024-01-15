package org.tasker.updates.service;

import org.tasker.common.dto.DefaultResponse;
import org.tasker.common.dto.LoginResponse;
import org.tasker.common.dto.VerifyTokenResponse;
import org.tasker.updates.models.dto.LoginRequest;
import org.tasker.updates.models.dto.RegisterRequest;
import org.tasker.updates.models.dto.VerifyTokenRequest;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<DefaultResponse> registerNewUser(RegisterRequest registerRequest);

    Mono<LoginResponse> loginUser(LoginRequest loginRequest);

    Mono<VerifyTokenResponse> verifyToken(VerifyTokenRequest verifyTokenRequest);
}
