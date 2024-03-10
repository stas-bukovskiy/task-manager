package org.tasker.task.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.models.commands.DeleteInvitationCommand;
import org.tasker.common.models.commands.InviteUsersCommand;
import org.tasker.common.models.commands.ReviewInvitationCommand;
import org.tasker.common.models.domain.UserAggregate;
import org.tasker.task.exception.ItemNotFoundException;
import org.tasker.task.service.BoardAggService;
import org.tasker.task.service.InvitationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final EventStoreDB eventStore;
    private final BoardAggService boardService;

    @Override
    public Mono<Void> inviteUsersToBoard(InviteUsersCommand command) {
        return boardService.getBoardAgg(command.boardId(), command.fromUserId())
                .zipWith(eventStore.exists(command.fromUserId(), UserAggregate.AGGREGATE_TYPE)
                        .handle((exists, sink) -> {
                            if (!exists) {
                                sink.error(new ItemNotFoundException("User not found for id: " + command.fromUserId()));
                            } else {
                                sink.next(true);
                            }
                        })
                        .flatMapMany(ignored2 -> Flux.fromIterable(command.toUserIds())
                                .publishOn(Schedulers.boundedElastic())
                                .map(userId -> {
                                    var isExist = eventStore.exists(userId, UserAggregate.AGGREGATE_TYPE).block();
                                    return Tuples.of(userId, isExist == Boolean.TRUE);
                                })
                                .filter(tuple -> {
                                    if (tuple.getT2())
                                        return true;

                                    log.warn("Skipped user invitation: user {} does not exist", tuple.getT1());
                                    return false;
                                })
                                .map(Tuple2::getT1)
                        )
                        .collectList())
                .map(tuple -> {
                    final var board = tuple.getT1();
                    final var existedUserIds = tuple.getT2().stream()
                            .filter(userId -> !board.getInvitedIds().contains(userId));

                    existedUserIds.forEach(board::inviteUser);
                    return board;
                })
                .flatMap(eventStore::save)
                .then();
    }

    @Override
    public Mono<Void> reviewInvitation(ReviewInvitationCommand command) {
        return boardService.getBoardAgg(command.boardId())
                .filter(board -> board.getInvitedIds().contains(command.userId()))
                .zipWith(eventStore.exists(command.userId())
                        .handle((exists, sink) -> {
                            if (!exists) {
                                sink.error(new ItemNotFoundException("User not found for id: " + command.userId()));
                            } else {
                                sink.next(true);
                            }
                        }))
                .map(tuple -> {
                    final var board = tuple.getT1();

                    board.reviewInvitation(command.userId(), command.isAccepted());
                    return board;
                })
                .flatMap(eventStore::save);
    }

    @Override
    public Mono<Void> deleteInvitation(DeleteInvitationCommand command) {
        return boardService.getBoardAgg(command.boardId(), command.ownerId())
                .map(board -> {
                    board.deleteIntimation(command.userId());
                    return board;
                })
                .flatMap(eventStore::save)
                .then();
    }

}
