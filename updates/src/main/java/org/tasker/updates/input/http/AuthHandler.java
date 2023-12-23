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
import org.tasker.updates.models.dto.RegisterRequest;
import org.tasker.updates.service.AuthService;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthHandler {

    private final Validator validator;
    private final AuthService authService;

    public Mono<ServerResponse> register(ServerRequest request) {
        Mono<RegisterRequest> registerRequestMono = request.bodyToMono(RegisterRequest.class);

        return registerRequestMono
                .doOnNext(this::validate)
//                .flatMap(authService::registerNewUser)
                .then(Mono.defer(() -> ServerResponse.accepted().build()));
    }

    private void validate(RegisterRequest registerRequest) {
        Errors errors = new BeanPropertyBindingResult(registerRequest, "register_request");
        validator.validate(registerRequest, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }

}
