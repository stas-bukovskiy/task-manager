package org.tasker.updates.input.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.tasker.updates.models.dto.LoginRequest;
import org.tasker.updates.models.dto.RegisterRequest;
import org.tasker.updates.models.dto.VerifyTokenRequest;
import org.tasker.updates.service.AuthService;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthHandler {

    private final Validator validator;
    private final AuthService authService;

    public Mono<ServerResponse> registerNewUser(ServerRequest request) {
        Mono<RegisterRequest> registerRequestMono = request.bodyToMono(RegisterRequest.class);

        return registerRequestMono
                .doOnNext(r -> validate(r, "register_request"))
                .flatMap(authService::registerNewUser)
                .flatMap(response -> ServerResponse.status(response.httpCode()).bodyValue(response));
    }

    public Mono<ServerResponse> loginUser(ServerRequest request) {
        Mono<LoginRequest> registerRequestMono = request.bodyToMono(LoginRequest.class);

        return registerRequestMono
                .doOnNext(r -> validate(r, "login_request"))
                .flatMap(authService::loginUser)
                .flatMap(response -> ServerResponse.status(response.httpCode()).bodyValue(response));
    }

    public Mono<ServerResponse> verifyToken(ServerRequest request) {
        Mono<VerifyTokenRequest> tokenMono = request.bodyToMono(VerifyTokenRequest.class);

        return tokenMono
                .doOnNext(r -> validate(r, "verify_token_request"))
                .flatMap(authService::verifyToken)
                .flatMap(response -> ServerResponse.status(response.httpCode()).bodyValue(response));
    }

    private void validate(Object target, String objectName) {
        Errors errors = new BeanPropertyBindingResult(target, objectName);
        validator.validate(target, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }
}
