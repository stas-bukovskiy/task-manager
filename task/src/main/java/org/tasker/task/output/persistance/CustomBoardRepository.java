package org.tasker.task.output.persistance;

import org.tasker.task.model.domain.BoardDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomBoardRepository {

    Mono<Void> addInvitedId(String aggregateId, String invitedId, String processedEventId);

    Mono<Void> addJoinedId(String aggregateId, String joinedU, String processedEventId);

    Mono<Void> removeInvitedId(String aggregateId, String invitedUserId, String processedEventId);

    Mono<Void> removeJoinedId(String aggregateId, String memberId, String processedEventId);

    Flux<BoardDocument> findBoardsByUserId(String userId);

    Mono<BoardDocument> findBoardByUserId(String userId, String boardId);

    Mono<Void> updateTitle(String aggregateId, String title, String processedEventId);
}
