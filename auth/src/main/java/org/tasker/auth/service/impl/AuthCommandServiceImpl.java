package org.tasker.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tasker.auth.exceptions.AlreadyExistsException;
import org.tasker.auth.models.domain.UserAggregate;
import org.tasker.auth.models.domain.UsernameEmailReservation;
import org.tasker.auth.output.persistance.UserReservationRepository;
import org.tasker.auth.service.AuthCommandService;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.models.commands.RegisterNewUserCommand;
import org.tasker.common.models.commands.UpdateUserCommand;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCommandServiceImpl implements AuthCommandService {

    private final EventStoreDB eventStore;
    private final UserReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Mono<Void> handle(RegisterNewUserCommand command) {
        final var password = passwordEncoder.encode(command.password());

        final var aggregateID = UUID.randomUUID().toString();
        final var aggregate = new UserAggregate(aggregateID);
        aggregate.createUser(command.username(), command.email(), password, command.firstName(), command.lastName());

        return reservationRepository.existsByUsernameOrEmail(command.username(), command.email())
                .handle((exists, sink) -> {
                    if (exists) {
                        sink.error(new AlreadyExistsException("Username or email already taken"));
                    } else {
                        sink.next(false);
                    }
                })
                .then(eventStore.save(aggregate))
                .then(reservationRepository.save(UsernameEmailReservation.builder()
                        .aggregateId(aggregateID)
                        .email(command.email())
                        .username(command.username())
                        .build()))
                .then();
    }

    @Override
    public Mono<Void> handle(UpdateUserCommand command) {
        return reservationRepository.existsByUsernameAndAggregateIdIsNot(command.username(), command.aggregateID())
                .handle((exists, sink) -> {
                    if (exists) {
                        sink.error(new AlreadyExistsException("Username already taken"));
                    } else {
                        sink.next(false);
                    }
                })
                .then(eventStore.load(command.aggregateID(), UserAggregate.class))
                .map(aggregate -> {
                    aggregate.updateUserInfo(command.username(), command.firstName(), command.lastName());
                    return aggregate;
                })
                .flatMap(eventStore::save)
                .then(reservationRepository.updateUsername(command.aggregateID(), command.username()));
    }
}
