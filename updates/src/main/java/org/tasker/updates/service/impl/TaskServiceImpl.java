package org.tasker.updates.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.queries.GetStatisticQuery;
import org.tasker.common.models.response.GetStatisticResponse;
import org.tasker.updates.output.event.TaskCommunicator;
import org.tasker.updates.service.TaskService;
import reactor.core.publisher.Mono;

@Service("updatesTaskService")
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskCommunicator communicator;

    @Override
    public Mono<GetStatisticResponse> getUserStatistic(String userAggregateId) {
        return communicator.publishAndReceive(
                GetStatisticQuery.QUERY_NAME,
                GetStatisticQuery.builder()
                        .userAggregateId(userAggregateId)
                        .build()
        ).map(bytes -> SerializerUtils.deserializeFromJsonBytes(bytes, GetStatisticResponse.class));
    }
}
