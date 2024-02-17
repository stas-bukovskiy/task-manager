package org.tasker.task.service;

import org.tasker.common.models.dto.TaskStatistic;
import reactor.core.publisher.Mono;

public interface TaskService {
    Mono<TaskStatistic> getStatistic(String userAggregateId);
}
