package org.tasker.updates.input.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.tasker.common.models.response.Response;
import org.tasker.updates.models.request.LoginRequest;
import org.tasker.updates.models.request.RegisterRequest;
import org.tasker.updates.models.request.VerifyTokenRequest;
import org.tasker.updates.models.response.ErrorMessages;
import org.tasker.updates.service.AuthService;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthHandler {

    private final Validator validator;
    private final AuthService authService;

    private static ResponseStatusException mapToResponseError(Response<?> response) {
        if (response.getHttpCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorMessages.INTERNAL_SERVER_ERROR
            );
        }
        return new ResponseStatusException(
                HttpStatus.valueOf(response.getHttpCode()),
                response.getMessage()
        );
    }

    public Mono<ServerResponse> registerNewUser(ServerRequest request) {
        Mono<RegisterRequest> registerRequestMono = request.bodyToMono(RegisterRequest.class);

        return registerRequestMono
                .doOnNext(r -> validate(r, "register_request"))
                .flatMap(authService::registerNewUser)
                .onErrorMap(ex -> {
                    log.error("Error while registering new user", ex);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR);
                })
                .flatMap(response -> {
                    if (response.getHttpCode() == HttpStatus.CREATED.value()) {
                        return ServerResponse.status(response.getHttpCode()).build();
                    }

                    return Mono.error(mapToResponseError(response));
                });
    }

    public Mono<ServerResponse> loginUser(ServerRequest request) {
        Mono<LoginRequest> registerRequestMono = request.bodyToMono(LoginRequest.class);

        return registerRequestMono
                .doOnNext(r -> validate(r, "login_request"))
                .flatMap(authService::loginUser)
                .onErrorMap(ex -> {
                    log.error("Error while login user", ex);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR);
                })
                .flatMap(response -> {
                    if (response.getHttpCode() == HttpStatus.OK.value()) {
                        return ServerResponse.status(response.getHttpCode()).bodyValue(Map.of(
                                "token", response.getData()
                        ));
                    }

                    return Mono.error(mapToResponseError(response));
                });
    }

    private void validate(Object target, String objectName) {
        Errors errors = new BeanPropertyBindingResult(target, objectName);
        validator.validate(target, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }

    public Mono<ServerResponse> verifyToken(ServerRequest request) {
        Mono<VerifyTokenRequest> tokenMono = request.bodyToMono(VerifyTokenRequest.class);

        return tokenMono
                .doOnNext(r -> validate(r, "verify_token_request"))
                .flatMap(authService::verifyToken)
                .onErrorMap(ex -> {
                    log.error("Error while verify token", ex);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR);
                })
                .flatMap(response -> {
                    if (response.getHttpCode() == HttpStatus.OK.value()) {
                        return ServerResponse.status(response.getHttpCode()).bodyValue(Map.of(
                                "user_id", response.getData()
                        ));
                    }

                    return Mono.error(mapToResponseError(response));
                });
    }
}
