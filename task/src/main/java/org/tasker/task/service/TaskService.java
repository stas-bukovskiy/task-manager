package org.tasker.task.service;

import org.tasker.common.models.commands.*;
import org.tasker.common.models.dto.TaskDto;
import org.tasker.common.models.dto.TaskStatistic;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TaskService {
    Mono<TaskStatistic> getStatistic(String userAggregateId);

    Mono<List<TaskDto>> getTasks(String userId, String boardId);

    Mono<TaskDto> getTask(String userId, String taskId);

    Mono<Void> createTask(CreateTaskCommand command);

    Mono<Void> updateTaskInfo(UpdateTaskInfoCommand command);

    Mono<Void> updateTaskStatus(UpdateTaskStatusCommand command);

    Mono<Void> deleteAssignee(DeleteAssigneeCommand command);

    Mono<Void> addAssignee(AddAssigneeCommand command);

    Mono<Void> deleteTask(DeleteTaskCommand command);
}
