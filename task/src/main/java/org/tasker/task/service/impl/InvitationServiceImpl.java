package org.tasker.task.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.models.commands.InviteUsersCommand;
import org.tasker.common.models.domain.InvitationAggregate;
import org.tasker.common.models.domain.UserAggregate;
import org.tasker.task.model.domain.BoardAggregate;
import org.tasker.task.service.InvitationService;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final EventStoreDB eventStore;

    @Override
    public Mono<Void> inviteUsersToBoard(InviteUsersCommand command) {
        return Mono.just(command.toUserIds() == null ? new ArrayList<Tuple2<String, InvitationAggregate>>() :
                        command.toUserIds().stream()
                                .filter(toUserId -> !toUserId.equals(command.fromUserId()))
                                .map(toUserId -> {
                                    final var aggregateId = UUID.randomUUID().toString();
                                    return Tuples.of(toUserId, new InvitationAggregate(aggregateId));
                                })
                                .toList())
                .zipWith(eventStore.load(command.boardId(), BoardAggregate.class), (invitationAggs, board) ->
                        Tuples.of(invitationAggs, board.getTitle()))
                .zipWith(eventStore.load(command.fromUserId(), UserAggregate.class), (tuple, fromUser) ->
                        Tuples.of(tuple.getT1(), tuple.getT2(), fromUser.getUsername()))
                .flatMapIterable(tuple -> {
                    final var invitationAggs = tuple.getT1();
                    final var boardTitle = tuple.getT2();
                    final var fromUsername = tuple.getT3();

                    return invitationAggs.stream()
                            .map(invAggTuple -> {
                                final var toUserId = invAggTuple.getT1();
                                final var invitationAggregate = invAggTuple.getT2();

                                invitationAggregate.createInvitation(boardTitle, command.boardId(), fromUsername, command.fromUserId(), toUserId);
                                return invitationAggregate;
                            })
                            .toList();
                })
                .flatMap(eventStore::save)
                .doOnError(e -> log.error("Error while saving invitation", e))
                .doOnNext(agg -> log.info("Invitation saved: {}", agg))
                .then();
    }

}
