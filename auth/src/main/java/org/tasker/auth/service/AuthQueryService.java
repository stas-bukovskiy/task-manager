package org.tasker.auth.service;

import org.tasker.common.models.queries.LoginUserQuery;
import org.tasker.common.models.queries.VerifyTokenQuery;
import reactor.core.publisher.Mono;

public interface AuthQueryService {

    Mono<String> handle(LoginUserQuery command);

    Mono<String> handle(VerifyTokenQuery command);
}
