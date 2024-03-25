package org.tasker.task.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.exception.ItemNotFoundException;
import org.tasker.common.exceptions.NotPermittedException;
import org.tasker.common.models.commands.*;
import org.tasker.common.models.domain.TaskAggregate;
import org.tasker.common.models.domain.TaskDocument;
import org.tasker.common.models.domain.UserAggregate;
import org.tasker.common.models.dto.TaskDto;
import org.tasker.common.models.dto.TaskStatistic;
import org.tasker.common.models.enums.TaskStatus;
import org.tasker.task.mapper.TaskMapper;
import org.tasker.task.output.persistance.TaskRepository;
import org.tasker.task.service.BoardAggService;
import org.tasker.task.service.TaskService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final EventStoreDB eventStore;
    private final BoardAggService boardService;
    private final TaskRepository taskRepository;

    @Override
    public Mono<TaskStatistic> getStatistic(String userAggregateId) {
        // TODO: implement when task service is ready
        var tempData = TaskStatistic.TaskStatisticByStatus.builder().build();
        return Mono.just(TaskStatistic.builder()
                .overallTaskStatistic(tempData)
                .assignedTaskStatistic(tempData)
                .build());
    }

    @Override
    public Mono<List<TaskDto>> getTasks(String userId, String boardId) {
        return boardService.getBoardAgg(boardId, userId)
                .thenMany(taskRepository.findByBoardId(boardId))
                .map(TaskMapper::fromDocToDTO)
                .collectList();
    }

    @Override
    public Mono<TaskDto> getTask(String userId, String taskId) {
        return getTaskDoc(taskId, userId, "get")
                .map(TaskMapper::fromDocToDTO);
    }

    @Override
    public Mono<Void> createTask(CreateTaskCommand command) {
        return boardService.getBoardAgg(command.boardId())
                .handle((board, sink) -> {
                    if (board.getOwnerId().equals(command.userId()) || board.getJoinedIds().contains(command.userId())) {
                        sink.next(board);
                    } else {
                        sink.error(new NotPermittedException("User is not permitted to create task"));
                    }
                }).map(ignored -> {
                    final var aggregateId = UUID.randomUUID().toString();
                    var task = new TaskAggregate(aggregateId);
                    task.createTask(command.boardId(), command.title(), command.description(), command.startDate(), command.dueDate(), command.estimatedTime(), command.priority(), command.userId());
                    if (command.status() != TaskStatus.TODO) {
                        task.updateStatus(command.status());
                    }
                    return task;
                }).flatMap(task -> addAssigneeEvents(task, new HashSet<>(command.assigneeIds())))
                .flatMap(eventStore::save);
    }

    @Override
    public Mono<Void> updateTaskInfo(UpdateTaskInfoCommand command) {
        return getTaskAgg(command.taskId(), command.userId(), "update info")
                .map(task -> {
                    task.updateTaskInfo(command.title(), command.description(), command.startDate(), command.dueDate(), command.estimatedTime(), command.priority());
                    return task;
                })
                .flatMap(eventStore::save);
    }

    @Override
    public Mono<Void> updateTaskStatus(UpdateTaskStatusCommand command) {
        return getTaskAgg(command.taskId(), command.userId(), "update status")
                .map(task -> {
                    task.updateStatus(command.status());
                    return task;
                })
                .flatMap(eventStore::save);
    }

    @Override
    public Mono<Void> deleteAssignee(DeleteAssigneeCommand command) {
        return getTaskAgg(command.taskId(), command.userId(), "delete assignee")
                .map(task -> {
                    if (task.getAssigneeIds().contains(command.assigneeId())) {
                        task.deleteAssignee(command.assigneeId());
                    }
                    return task;
                })
                .flatMap(eventStore::save);
    }

    @Override
    public Mono<Void> addAssignee(AddAssigneeCommand command) {
        return getTaskDoc(command.taskId(), command.userId(), "add assignee")
                .flatMap(task -> {
                    if (task.getBoard().getJoinedIds() == null || !task.getBoard().getJoinedIds().contains(command.assigneeId())) {
                        return Mono.error(new NotPermittedException("User %s is already assigned to task %s", command.assigneeId(), command.taskId()));
                    }
                    return Mono.just(task);
                })
                .then(eventStore.load(command.taskId(), TaskAggregate.class))
                .flatMap(task -> addAssigneeEvents(task, Set.of(command.assigneeId())))
                .flatMap(eventStore::save);
    }

    @Override
    public Mono<Void> deleteTask(DeleteTaskCommand command) {
        return getTaskAgg(command.taskId(), command.userId(), "delete")
                .map(task -> {
                    task.deleteTask();
                    return task;
                })
                .flatMap(eventStore::save);
    }

    private Mono<TaskAggregate> addAssigneeEvents(TaskAggregate task, Set<String> assignedIds) {
        return Flux.fromIterable(assignedIds)
                .publishOn(Schedulers.boundedElastic())
                .filter(assigneeId -> !task.getAssigneeIds().contains(assigneeId))
                .filter(assigneeId -> {
                    final var isExist = eventStore.exists(assigneeId, UserAggregate.AGGREGATE_TYPE).block();
                    if (isExist == Boolean.FALSE) {
                        log.info("Skipped assignee adding: user {} does not exist", assigneeId);
                        return false;
                    }

                    return true;
                })
                .collectList()
                .map(assigneeIds -> {
                    assigneeIds.forEach(task::addAssignee);
                    return task;
                });
    }

    private Mono<TaskAggregate> getTaskAgg(String taskId, String userId, String actionName) {
        return getTaskDoc(taskId, userId, actionName)
                .then(eventStore.load(taskId, TaskAggregate.class));
    }

    private Mono<TaskDocument> getTaskDoc(String taskId, String userId, String actionName) {
        return taskRepository.findByAggregateId(taskId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Task %s not found", taskId)))
                .flatMap(task -> {
                    if (task.getBoard() != null
                            && (task.getBoard().getOwnerId().equals(userId)
                            || (task.getBoard().getJoinedIds() != null && task.getBoard().getJoinedIds().contains(userId)))) {
                        return Mono.just(task);
                    } else {
                        return Mono.error(new NotPermittedException("User %s is not permitted to %s task %s", userId, actionName, taskId));
                    }
                });
    }

}
