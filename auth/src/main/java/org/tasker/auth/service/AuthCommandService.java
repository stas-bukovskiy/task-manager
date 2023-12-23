package org.tasker.auth.service;

import org.tasker.common.models.command.RegisterNewUserCommand;
import reactor.core.publisher.Mono;

public interface AuthCommandService {

    Mono<String> handle(RegisterNewUserCommand command);

}
