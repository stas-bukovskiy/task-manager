package org.tasker.task.service;

import org.tasker.common.models.dto.BoardStatistic;
import reactor.core.publisher.Mono;

public interface BoardService {
    Mono<BoardStatistic> getStatistic(String userAggregateId);
}
