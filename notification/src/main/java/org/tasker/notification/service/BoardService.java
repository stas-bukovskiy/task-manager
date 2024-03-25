package org.tasker.notification.service;

import org.tasker.common.models.domain.BoardDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BoardService {
    Flux<String> getBoardOnlineUserIds(String aggregateId);

    Mono<BoardDocument> getBoard(String aggregateId);
}
