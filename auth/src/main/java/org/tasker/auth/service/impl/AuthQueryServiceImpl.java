package org.tasker.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tasker.auth.exceptions.InvalidCredentialsException;
import org.tasker.auth.models.domain.UserDocument;
import org.tasker.auth.output.persistance.UserRepository;
import org.tasker.auth.service.AuthQueryService;
import org.tasker.auth.service.JWTService;
import org.tasker.common.models.queries.GetUserQuery;
import org.tasker.common.models.queries.LoginUserQuery;
import org.tasker.common.models.queries.VerifyTokenQuery;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthQueryServiceImpl implements AuthQueryService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public Mono<String> handle(LoginUserQuery query) {
        return userRepository.findByEmailOrUsername(query.login(), query.login())
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("User not found or invalid credentials")))
                .handle((userDoc, sink) -> {
                    if (passwordEncoder.matches(query.password(), userDoc.getPassword())) {
                        final var token = jwtService.generateToken(userDoc.getAggregateId());
                        sink.next(token);
                    } else {
                        sink.error(new InvalidCredentialsException("User not found or invalid credentials"));
                    }
                });
    }

    public Mono<String> handle(VerifyTokenQuery query) {
        return Mono.just(query.token())
                .map(jwtService::verifyToken)
                .onErrorMap(e -> new InvalidCredentialsException("Invalid token"));
    }

    @Override
    public Mono<List<UserDocument>> handle(GetUserQuery query) {
        if (query.aggregateId() != null && StringUtils.hasText(query.aggregateId())) {
            return userRepository.findByAggregateId(query.aggregateId())
                    .map(List::of);
        }

        return userRepository.findAllBySearching(query.search())
                .collectList();
    }
}
