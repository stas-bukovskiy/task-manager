package org.tasker.notification.service;

import org.tasker.common.models.domain.TaskDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskService {
    Flux<String> getTaskOnlineUserIds(String aggregateId);

    Mono<TaskDocument> getTask(String aggregateId);
}
