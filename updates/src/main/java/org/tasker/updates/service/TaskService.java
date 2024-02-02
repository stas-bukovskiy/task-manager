package org.tasker.updates.service;

import org.tasker.common.models.response.GetStatisticResponse;
import reactor.core.publisher.Mono;

public interface TaskService {
    Mono<GetStatisticResponse> getUserStatistic(String userAggregateId);
}
