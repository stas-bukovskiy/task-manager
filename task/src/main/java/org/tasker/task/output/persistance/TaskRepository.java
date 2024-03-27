package org.tasker.task.output.persistance;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.tasker.common.models.domain.TaskDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TaskRepository extends ReactiveMongoRepository<TaskDocument, String>, CustomTaskRepository {
    Mono<Void> deleteByAggregateId(String aggregateId);

    Mono<Boolean> existsByAggregateId(String aggregateId);

    Flux<TaskDocument> findByBoardId(String boardId);

}
