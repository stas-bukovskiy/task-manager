package org.tasker.notification.output.persistance;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.tasker.common.models.domain.BoardDocument;
import reactor.core.publisher.Mono;

@Repository("notificationBoardRepository")
public interface BoardRepository extends ReactiveMongoRepository<BoardDocument, String> {
    Mono<BoardDocument> findByAggregateId(String aggregateId);
}
