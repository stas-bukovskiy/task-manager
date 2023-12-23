package org.tasker.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.auth.service.AuthCommandService;
import org.tasker.common.models.command.RegisterNewUserCommand;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCommandServiceImpl implements AuthCommandService {

    private final UserEventStore eventStore;

    @Override
    public Mono<String> handle(RegisterNewUserCommand command) {
//        final var aggregate = new UserAggregate(command.aggregateID());
//        aggregate.registerUser(command.username(), command.email(), command.password(), command.firstName(), command.lastName());
//
//        log.info("(RegisterNewUserCommand) aggregate: {}", aggregate);
//        eventStore.saveAll(aggregate.getChanges()).subscribe();
//        return Mono.just(aggregate.getId());
        return Mono.just(UUID.randomUUID().toString());
    }

}
