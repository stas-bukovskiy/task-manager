package org.tasker.task.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.models.dto.TaskStatistic;
import org.tasker.task.service.TaskService;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Override
    public Mono<TaskStatistic> getStatistic(String userAggregateId) {
        // TODO: implement when task service is ready
        var tempData = TaskStatistic.TaskStatisticByStatus.builder().build();
        return Mono.just(TaskStatistic.builder()
                .overallTaskStatistic(tempData)
                .assignedTaskStatistic(tempData)
                .build());
    }

}
