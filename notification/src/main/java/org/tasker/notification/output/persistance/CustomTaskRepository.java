package org.tasker.notification.output.persistance;


import org.tasker.common.models.domain.TaskDocument;
import reactor.core.publisher.Mono;

public interface CustomTaskRepository {
    Mono<TaskDocument> findByAggregateId(String aggregateId);
}
