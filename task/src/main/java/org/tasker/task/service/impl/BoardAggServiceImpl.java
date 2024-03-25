package org.tasker.task.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.exception.ItemNotFoundException;
import org.tasker.common.exceptions.NotPermittedException;
import org.tasker.common.models.domain.BoardAggregate;
import org.tasker.task.service.BoardAggService;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardAggServiceImpl implements BoardAggService {

    private final EventStoreDB eventStore;

    @Override
    public Mono<BoardAggregate> getBoardAgg(String boardId, String userId) {
        return getBoardAgg(boardId)
                .handle((board, sink) -> {
                    if (!board.getOwnerId().equals(userId)) {
                        sink.error(new NotPermittedException("User %s is not owner for board %s", userId, board.getId()));
                        return;
                    }

                    sink.next(board);
                });
    }

    @Override
    public Mono<BoardAggregate> getBoardAgg(String boardId) {
        return eventStore.exists(boardId, BoardAggregate.AGGREGATE_TYPE)
                .handle((exists, sink) -> {
                    if (!exists) {
                        sink.error(new ItemNotFoundException("Board %s not found", boardId));
                    } else {
                        sink.next(true);
                    }
                })
                .flatMap(ignored -> eventStore.load(boardId, BoardAggregate.class));
    }
}
