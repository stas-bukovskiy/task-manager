package org.tasker.updates.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.dto.DefaultResponse;
import org.tasker.common.dto.LoginResponse;
import org.tasker.common.dto.VerifyTokenResponse;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.commands.RegisterNewUserCommand;
import org.tasker.common.models.queries.LoginUserQuery;
import org.tasker.common.models.queries.VerifyTokenQuery;
import org.tasker.updates.models.dto.LoginRequest;
import org.tasker.updates.models.dto.RegisterRequest;
import org.tasker.updates.models.dto.VerifyTokenRequest;
import org.tasker.updates.output.event.AuthPublisher;
import org.tasker.updates.service.AuthService;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthPublisher publisher;

    @Override
    public Mono<DefaultResponse> registerNewUser(RegisterRequest registerRequest) {
        return publisher.publishAndReceive(
                RegisterNewUserCommand.COMMAND_NAME,
                RegisterNewUserCommand.builder()
                        .username(registerRequest.username())
                        .email(registerRequest.email())
                        .password(registerRequest.password())
                        .firstName(registerRequest.firstName())
                        .lastName(registerRequest.lastName())
                        .build()
        ).map(bytes -> SerializerUtils.deserializeFromJsonBytes(bytes, DefaultResponse.class));
    }

    @Override
    public Mono<LoginResponse> loginUser(LoginRequest loginRequest) {
        return publisher.publishAndReceive(
                LoginUserQuery.QUERY_NAME,
                LoginUserQuery.builder()
                        .login(loginRequest.login())
                        .password(loginRequest.password())
                        .build()
        ).map(bytes -> SerializerUtils.deserializeFromJsonBytes(bytes, LoginResponse.class));
    }

    @Override
    public Mono<VerifyTokenResponse> verifyToken(VerifyTokenRequest verifyTokenRequest) {
        return publisher.publishAndReceive(
                VerifyTokenQuery.QUERY_NAME,
                VerifyTokenQuery.builder()
                        .token(verifyTokenRequest.token())
                        .build()
        ).map(bytes -> SerializerUtils.deserializeFromJsonBytes(bytes, VerifyTokenResponse.class));
    }


}
