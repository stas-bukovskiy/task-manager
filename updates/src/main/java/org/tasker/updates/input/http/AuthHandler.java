package org.tasker.updates.input.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.tasker.common.models.response.Response;
import org.tasker.updates.models.request.LoginRequest;
import org.tasker.updates.models.request.RegisterRequest;
import org.tasker.updates.models.request.VerifyTokenRequest;
import org.tasker.updates.models.response.ErrorMessages;
import org.tasker.updates.service.AuthService;
import org.tasker.updates.service.ValidationService;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.tasker.updates.models.response.ErrorMessages.UNKNOWN_ERROR;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthHandler {

    private final ValidationService validator;
    private final AuthService authService;

    public Mono<ServerResponse> registerNewUser(ServerRequest request) {
        Mono<RegisterRequest> registerRequestMono = request.bodyToMono(RegisterRequest.class);
        return registerRequestMono
                .doOnNext(r -> validator.validate(r, "register_request"))
                .flatMap(authService::registerNewUser)
                .flatMap(response -> {
                    if (response.getHttpCode() == HttpStatus.CREATED.value()) {
                        return ServerResponse.status(response.getHttpCode()).build();
                    }

                    return Mono.error(mapToResponseError(response));
                })
                .onErrorResume(this::sendErrorResponse);
    }

    public Mono<ServerResponse> loginUser(ServerRequest request) {
        Mono<LoginRequest> registerRequestMono = request.bodyToMono(LoginRequest.class);
        return registerRequestMono
                .doOnNext(r -> validator.validate(r, "login_request"))
                .flatMap(authService::loginUser)
                .flatMap(response -> {
                    if (response.getHttpCode() == HttpStatus.OK.value()) {
                        return ServerResponse.status(response.getHttpCode()).bodyValue(response);
                    }

                    return Mono.error(mapToResponseError(response));
                })
                .onErrorResume(this::sendErrorResponse);
    }

    public Mono<ServerResponse> verifyToken(ServerRequest request) {
        Mono<VerifyTokenRequest> tokenMono = request.bodyToMono(VerifyTokenRequest.class);
        return tokenMono
                .doOnNext(r -> validator.validate(r, "verify_token_request"))
                .flatMap(authService::verifyToken)
                .flatMap(response -> {
                    if (response.getHttpCode() == HttpStatus.OK.value()) {
                        return ServerResponse.status(response.getHttpCode()).bodyValue(Map.of(
                                "user_id", response.getData()
                        ));
                    }

                    return Mono.error(mapToResponseError(response));
                })
                .onErrorResume(this::sendErrorResponse);
    }

    private ResponseStatusException mapToResponseError(Response<?> response) {
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


    private Mono<ServerResponse> sendErrorResponse(Throwable throwable) {
        if (throwable instanceof ResponseStatusException responseStatusException) {
            return ServerResponse.status(responseStatusException.getStatusCode()).bodyValue(
                    Map.of("message", responseStatusException.getReason() == null ? UNKNOWN_ERROR : responseStatusException.getReason())
            );
        }
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(
                Map.of("message", ErrorMessages.INTERNAL_SERVER_ERROR)
        );
    }

}
