package org.tasker.task.output.persistance;

import org.tasker.common.models.domain.TaskDocument;
import reactor.core.publisher.Mono;

public interface CustomTaskRepository {
    Mono<TaskDocument> findByAggregateId(String aggregateId);

    Mono<TaskDocument> updateTaskInfo(TaskDocument taskDoc);

    Mono<TaskDocument> updateTaskStatus(TaskDocument taskDoc);

    Mono<TaskDocument> addAssigneeId(TaskDocument taskDoc, String assigneeId);

    Mono<TaskDocument> deleteAssigneeId(TaskDocument taskDoc, String assigneeId);
}
