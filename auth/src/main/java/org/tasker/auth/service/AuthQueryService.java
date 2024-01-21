package org.tasker.auth.service;

import org.tasker.auth.models.domain.UserDocument;
import org.tasker.common.models.queries.GetUserQuery;
import org.tasker.common.models.queries.LoginUserQuery;
import org.tasker.common.models.queries.VerifyTokenQuery;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AuthQueryService {

    Mono<String> handle(LoginUserQuery query);

    Mono<String> handle(VerifyTokenQuery query);

    Mono<List<UserDocument>> handle(GetUserQuery query);
}
