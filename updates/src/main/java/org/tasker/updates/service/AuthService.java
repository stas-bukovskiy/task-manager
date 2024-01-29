package org.tasker.updates.service;

import org.tasker.common.models.response.DefaultResponse;
import org.tasker.common.models.response.LoginUserResponse;
import org.tasker.updates.models.request.LoginRequest;
import org.tasker.updates.models.request.RegisterRequest;
import org.tasker.updates.models.request.VerifyTokenRequest;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<DefaultResponse> registerNewUser(RegisterRequest registerRequest);

    Mono<LoginUserResponse> loginUser(LoginRequest loginRequest);

    Mono<DefaultResponse> verifyToken(VerifyTokenRequest verifyTokenRequest);
}
