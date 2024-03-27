package org.tasker.task.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.exception.ItemNotFoundException;
import org.tasker.common.exceptions.NotPermittedException;
import org.tasker.common.models.commands.CreateBoardCommand;
import org.tasker.common.models.commands.DeleteTaskCommand;
import org.tasker.common.models.commands.InviteUsersCommand;
import org.tasker.common.models.domain.BoardAggregate;
import org.tasker.common.models.dto.BoardDto;
import org.tasker.common.models.dto.BoardStatistic;
import org.tasker.task.mapper.BoardMapper;
import org.tasker.task.output.persistance.BoardRepository;
import org.tasker.task.service.BoardAggService;
import org.tasker.task.service.BoardService;
import org.tasker.task.service.InvitationService;
import org.tasker.task.service.TaskService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final EventStoreDB eventStore;
    private final InvitationService invitationService;
    private final BoardRepository boardRepository;
    private final BoardAggService boardAggService;
    private final TaskService taskService;

    @Override
    public Mono<BoardStatistic> getStatistic(String userAggregateId) {
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
                        .toUserIds(command.invitedUserIds())
                        .build())).subscribe());
    }

    @Override
    public Mono<List<BoardDto>> getBoards(String userId) {
        return boardRepository.findBoardsByUserId(userId)
                .map(BoardMapper::fromDocToDto)
                .collectList();
    }

    @Override
    public Mono<BoardDto> getBoard(String userId, String boardId) {
        return boardRepository.findBoardByUserId(userId, boardId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException()))
                .map(BoardMapper::fromDocToDto);
    }

    @Override
    public Mono<Void> deleteBoard(String boardId, String userId) {
        return taskService.getTasks(userId, boardId)
                .flatMapMany(Flux::fromIterable)
                .flatMap(task -> taskService.deleteTask(DeleteTaskCommand.builder()
                        .taskId(task.aggregateId())
                        .userId(userId)
                        .build()))
                .then(boardAggService.getBoardAgg(boardId, userId))
                .flatMap(aggregate -> {
                    if (!aggregate.getOwnerId().equals(userId)) {
                        return Mono.error(new NotPermittedException());
                    }
                    aggregate.deleteBoard();
                    return eventStore.save(aggregate);
                });
    }

    @Override
    public Mono<Void> deleteMember(String boardId, String userId, String memberId) {
        return boardAggService.getBoardAgg(boardId, userId)
                .flatMap(aggregate -> {
                    if (!aggregate.getOwnerId().equals(userId)) {
                        return Mono.error(new NotPermittedException());
                    }
                    if (aggregate.getJoinedIds().contains(memberId)) {
                        aggregate.deleteMember(memberId);
                    }
                    return eventStore.save(aggregate);
                });
    }

    @Override
    public Mono<Void> updateBoard(String boardId, String userId, String title) {
        return boardAggService.getBoardAgg(boardId, userId)
                .flatMap(aggregate -> {
                    if (!aggregate.getOwnerId().equals(userId)) {
                        return Mono.error(new NotPermittedException());
                    }
                    aggregate.updateBoard(title);
                    return eventStore.save(aggregate);
                });
    }

}
