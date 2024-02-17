package org.tasker.task.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.models.commands.CreateBoardCommand;
import org.tasker.common.models.commands.InviteUsersCommand;
import org.tasker.common.models.domain.BoardAggregate;
import org.tasker.common.models.dto.BoardStatistic;
import org.tasker.task.service.BoardService;
import org.tasker.task.service.InvitationService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final EventStoreDB eventStore;
    private final InvitationService invitationService;

    @Override
    public Mono<BoardStatistic> getStatistic(String userAggregateId) {
        // TODO: implement when board service is ready
        return Mono.just(BoardStatistic.builder()
                .createdNum(1)
                .joinedNum(1)
                .build());
    }

    @Override
    public Mono<Void> createBoard(CreateBoardCommand command) {
        final var aggregateId = UUID.randomUUID().toString();
        return Mono.fromCallable(() -> {
                    final var aggregate = new BoardAggregate(aggregateId);

                    aggregate.createBoard(command.title(), command.ownerId());
                    return aggregate;
                }).flatMap(eventStore::save)
                .publishOn(Schedulers.immediate())
                .doOnSuccess(v -> Mono.defer(() -> invitationService.inviteUsersToBoard(InviteUsersCommand.builder()
                        .boardId(aggregateId)
                        .fromUserId(command.ownerId())
                        .toUserIds(command.invitedPeopleIds())
                        .build())).subscribe());
    }

}
