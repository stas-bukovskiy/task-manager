package org.tasker.task.output.persistance;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.tasker.task.model.domain.BoardDocument;
import reactor.core.publisher.Mono;

public interface BoardRepository extends ReactiveMongoRepository<BoardDocument, String>, CustomBoardRepository {
    Mono<BoardDocument> findByAggregateId(String aggregateId);
    Mono<Void> deleteByAggregateId(String aggregateId);
}
