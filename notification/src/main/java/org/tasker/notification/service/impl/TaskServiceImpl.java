package org.tasker.notification.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tasker.common.exception.ItemNotFoundException;
import org.tasker.common.models.domain.TaskDocument;
import org.tasker.notification.output.persistance.TaskRepository;
import org.tasker.notification.service.TaskService;
import org.tasker.notification.service.UserStatusService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service("notificationTaskService")
public class TaskServiceImpl implements TaskService {

    private final UserStatusService userStatusService;
    private final TaskRepository taskRepository;

    public TaskServiceImpl(@Qualifier("notificationUserStatusService") UserStatusService userStatusService,
                           @Qualifier("notificationTaskRepository") TaskRepository taskRepository) {
        this.userStatusService = userStatusService;
        this.taskRepository = taskRepository;
    }

    @Override
    public Flux<String> getTaskOnlineUserIds(String aggregateId) {
        return getTask(aggregateId)
                .flatMapMany(taskDocument -> {
                    Set<String> assigneeIds = new HashSet<>(taskDocument.getAssigneeIds());
                    assigneeIds.add(taskDocument.getBoard().getId());

                    return userStatusService.filterByOnlineStatus(assigneeIds, true);
                });
    }

    @Override
    public Mono<TaskDocument> getTask(String aggregateId) {
        return taskRepository.findByAggregateId(aggregateId)
                .repeatWhenEmpty(3, (retrySpec) -> retrySpec.delayElements(Duration.of(1, ChronoUnit.SECONDS)))
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Task %s not found", aggregateId)));
    }
}
