package org.tasker.task.input.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.Event;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.es.Projection;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.domain.TaskAggregate;
import org.tasker.common.models.enums.TaskStatus;
import org.tasker.common.models.event.*;
import org.tasker.common.output.event.EventsMessagingSpecs;
import org.tasker.task.mapper.TaskMapper;
import org.tasker.task.model.domain.TaskDocument;
import org.tasker.task.output.persistance.TaskRepository;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class TaskProjection extends Projection {

    private final static String[] ROUTING_KEYS = new String[]{
            TaskAggregate.AGGREGATE_TYPE + ".*",
    };

    private final TaskRepository taskRepository;
    private final EventStoreDB eventStore;


    public TaskProjection(TaskRepository taskRepository, EventStoreDB eventStore, EventsMessagingSpecs messagingSpecs,
                          Receiver receiver) {
        super(receiver, messagingSpecs, Map.of(
                TaskCreatedEvent.TASK_CREATED_V1, (event ->
                        taskRepository.existsByAggregateId(event.getAggregateId())
                                .handle((exists, sink) -> {
                                    if (exists) {
                                        log.info("task doc <{}> already exist with such aggregateId: {}", event.getId(), event.getAggregateId());
                                        sink.complete();
                                    } else {
                                        sink.next(false);
                                    }
                                })
                                .map(ignored -> {
                                    final var taskCreatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskCreatedEvent.class);
                                    return TaskDocument.builder()
                                            .aggregateId(taskCreatedEvent.getAggregateId())
                                            .title(taskCreatedEvent.getTitle())
                                            .description(taskCreatedEvent.getDescription())
                                            .startDate(taskCreatedEvent.getStartDate())
                                            .dueDate(taskCreatedEvent.getDueDate())
                                            .estimatedTime(taskCreatedEvent.getEstimatedTime())
                                            .priority(taskCreatedEvent.getPriority())
                                            .status(TaskStatus.TODO)
                                            .assigneeIds(Set.of())
                                            .boardId(taskCreatedEvent.getBoardId())
                                            .build();
                                }).flatMap(taskRepository::insert)
                                .doOnNext(inserted -> log.info("task info doc <{}> created for aggregateId: {}", inserted.getId(), inserted.getAggregateId()))
                                .then()),
                TaskInfoUpdatedEvent.TASK_INFO_UPDATED_V1, (event -> getByAggregateId(taskRepository, event.getAggregateId())
                        .flatMap(taskDoc -> {
                            final var taskInfoUpdatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskInfoUpdatedEvent.class);
                            taskDoc.setTitle(taskInfoUpdatedEvent.getTitle());
                            taskDoc.setDescription(taskInfoUpdatedEvent.getDescription());
                            taskDoc.setStartDate(taskInfoUpdatedEvent.getStartDate());
                            taskDoc.setDueDate(taskInfoUpdatedEvent.getDueDate());
                            taskDoc.setEstimatedTime(taskInfoUpdatedEvent.getEstimatedTime());
                            taskDoc.setPriority(taskInfoUpdatedEvent.getPriority());

                            return taskRepository.updateTaskInfo(taskDoc);
                        })
                        .doOnNext(updated -> log.info("task doc <{}> updated for aggregateId: {}", updated.getId(), updated.getAggregateId()))
                        .then()),
                TaskStatusUpdatedEvent.TASK_STATUS_UPDATED_V1, (event -> getByAggregateId(taskRepository, event.getAggregateId())
                        .flatMap(taskDoc -> {
                            final var taskStatusUpdatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskStatusUpdatedEvent.class);
                            taskDoc.setStatus(taskStatusUpdatedEvent.getStatus());
                            return taskRepository.updateTaskStatus(taskDoc);
                        })
                        .doOnNext(updated -> log.info("task status doc <{}> updated for aggregateId: {}", updated.getId(), updated.getAggregateId()))
                        .then()),
                TaskAssigneeAdded.TASK_ASSIGNEE_ADDED_V1, (event -> getByAggregateId(taskRepository, event.getAggregateId())
                        .flatMap(taskDoc -> {
                            final var taskAssigneeAdded = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskAssigneeAdded.class);
                            return taskRepository.addAssigneeId(taskDoc, taskAssigneeAdded.getAssigneeId());
                        })
                        .doOnNext(updated -> log.info("task assignee doc <{}> updated for aggregateId: {}", updated.getId(), updated.getAggregateId()))
                        .then()),
                TaskAssigneeDeleted.TASK_ASSIGNEE_DELETED_V1, (event -> getByAggregateId(taskRepository, event.getAggregateId())
                        .flatMap(taskDoc -> {
                            final var taskAssigneeDeleted = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskAssigneeDeleted.class);
                            return taskRepository.deleteAssigneeId(taskDoc, taskAssigneeDeleted.getAssigneeId());
                        })
                        .doOnNext(updated -> log.info("task assignee doc <{}> updated for aggregateId: {}", updated.getId(), updated.getAggregateId()))
                        .then()),
                TaskDeletedEvent.TASK_DELETED_V1, (event -> taskRepository.deleteByAggregateId(event.getAggregateId())
                        .doOnSuccess(v -> log.info("task doc deleted for aggregateId: {}", event.getAggregateId()))
                )
        ), TaskAggregate.AGGREGATE_TYPE, ROUTING_KEYS);

        this.taskRepository = taskRepository;
        this.eventStore = eventStore;
    }

    private static Mono<TaskDocument> getByAggregateId(TaskRepository taskRepository, String aggregateId) {
        return taskRepository.findByAggregateId(aggregateId)
                .repeatWhenEmpty(3, (retrySpec) -> retrySpec.delayElements(Duration.of(1, ChronoUnit.SECONDS)));
    }

    @Override
    protected Mono<Void> handleError(Throwable err, Event event) {
        return taskRepository.deleteByAggregateId(event.getAggregateId())
                .then(eventStore.load(event.getAggregateId(), TaskAggregate.class))
                .map(TaskMapper::fromAggToDoc)
                .flatMap(taskRepository::insert)
                .doOnSuccess(boardDoc -> log.info("successfully restored task doc <{}> for aggregateId: {}", boardDoc.getId(), boardDoc.getAggregateId()))
                .then();
    }

}
