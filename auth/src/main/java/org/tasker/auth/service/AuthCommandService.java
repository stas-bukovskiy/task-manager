package org.tasker.auth.service;

import org.tasker.common.models.commands.RegisterNewUserCommand;
import org.tasker.common.models.commands.UpdateUserCommand;
import reactor.core.publisher.Mono;

public interface AuthCommandService {

    Mono<Void> handle(RegisterNewUserCommand command);

    Mono<Void> handle(UpdateUserCommand command);

}
