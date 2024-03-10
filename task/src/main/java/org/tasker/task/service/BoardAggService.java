package org.tasker.task.service;

import org.tasker.common.models.domain.BoardAggregate;
import reactor.core.publisher.Mono;

public interface BoardAggService {
    Mono<BoardAggregate> getBoardAgg(String boardId, String userId);

    Mono<BoardAggregate> getBoardAgg(String boardId);
}
