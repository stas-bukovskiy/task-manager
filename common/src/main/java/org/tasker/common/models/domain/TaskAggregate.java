package org.tasker.common.models.domain;

import lombok.*;
import org.tasker.common.es.AggregateRoot;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.enums.TaskPriority;
import org.tasker.common.models.enums.TaskStatus;
import org.tasker.common.models.event.*;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TaskAggregate extends AggregateRoot {

    public static final String AGGREGATE_TYPE = "task_aggregate";

    private String boardId;
    private String title;
    private String description;
    private Date startDate;
    private Date dueDate;
    private int estimatedTime;
    private TaskPriority priority;
    private TaskStatus status;
    private Set<String> assigneeIds;
    private boolean isDeleted;

    public TaskAggregate(String id) {
        super(id, AGGREGATE_TYPE);
    }

    @Override
    public void when(Event event) {
        switch (event.getEventType()) {
            case TaskCreatedEvent.TASK_CREATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskCreatedEvent.class));
            case TaskStatusUpdatedEvent.TASK_STATUS_UPDATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskStatusUpdatedEvent.class));
            case TaskAssigneeAdded.TASK_ASSIGNEE_ADDED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskAssigneeAdded.class));
            case TaskInfoUpdatedEvent.TASK_INFO_UPDATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskInfoUpdatedEvent.class));
            case TaskAssigneeDeleted.TASK_ASSIGNEE_DELETED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskAssigneeDeleted.class));
            case TaskDeletedEvent.TASK_DELETED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskDeletedEvent.class));
        }
    }

    private void handle(TaskCreatedEvent taskCreatedEvent) {
        this.boardId = taskCreatedEvent.getBoardId();
        this.title = taskCreatedEvent.getTitle();
        this.description = taskCreatedEvent.getDescription();
        this.startDate = taskCreatedEvent.getStartDate();
        this.dueDate = taskCreatedEvent.getDueDate();
        this.estimatedTime = taskCreatedEvent.getEstimatedTime();
        this.priority = taskCreatedEvent.getPriority();
        this.status = TaskStatus.TODO;
        this.assigneeIds = Collections.synchronizedSet(new HashSet<>());
    }

    private void handle(TaskStatusUpdatedEvent taskCreatedEvent) {
        this.status = taskCreatedEvent.getStatus();
    }

    private void handle(TaskAssigneeAdded taskAssigneeAdded) {
        this.assigneeIds.add(taskAssigneeAdded.getAssigneeId());
    }

    private void handle(TaskInfoUpdatedEvent taskInfoUpdatedEvent) {
        this.title = taskInfoUpdatedEvent.getTitle();
        this.description = taskInfoUpdatedEvent.getDescription();
        this.startDate = taskInfoUpdatedEvent.getStartDate();
        this.dueDate = taskInfoUpdatedEvent.getDueDate();
        this.estimatedTime = taskInfoUpdatedEvent.getEstimatedTime();
        this.priority = taskInfoUpdatedEvent.getPriority();
    }

    public void createTask(String boardId, String title, String description, Date startDate, Date dueDate, int estimatedTime, TaskPriority priority, String createdBy) {
        final var data = TaskCreatedEvent.builder()
                .aggregateId(id)
                .boardId(boardId)
                .title(title)
                .description(description)
                .startDate(startDate)
                .dueDate(dueDate)
                .estimatedTime(estimatedTime)
                .priority(priority)
                .createdBy(createdBy)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(TaskCreatedEvent.TASK_CREATED_V1, dataBytes);
        this.apply(event);
    }

    public void updateStatus(TaskStatus status) {
        final var data = TaskStatusUpdatedEvent.builder()
                .aggregateId(id)
                .status(status)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(TaskStatusUpdatedEvent.TASK_STATUS_UPDATED_V1, dataBytes);
        this.apply(event);
    }

    public void addAssignee(String assigneeId) {
        final var data = TaskAssigneeAdded.builder()
                .aggregateId(id)
                .assigneeId(assigneeId)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(TaskAssigneeAdded.TASK_ASSIGNEE_ADDED_V1, dataBytes);
        this.apply(event);
    }

    private void handle(TaskAssigneeDeleted taskAssigneeDeleted) {
        this.assigneeIds.remove(taskAssigneeDeleted.getAssigneeId());
    }

    private void handle(TaskDeletedEvent ignored) {
        this.isDeleted = true;
    }

    public void updateTaskInfo(String title, String description, Date startDate, Date dueDate, int estimatedTime, TaskPriority priority) {
        final var data = TaskInfoUpdatedEvent.builder()
                .aggregateId(id)
                .title(title)
                .description(description)
                .startDate(startDate)
                .dueDate(dueDate)
                .estimatedTime(estimatedTime)
                .priority(priority)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(TaskInfoUpdatedEvent.TASK_INFO_UPDATED_V1, dataBytes);
        this.apply(event);
    }

    public void deleteAssignee(String assigneeId) {
        final var data = TaskAssigneeDeleted.builder()
                .aggregateId(id)
                .assigneeId(assigneeId)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(TaskAssigneeDeleted.TASK_ASSIGNEE_DELETED_V1, dataBytes);
        this.apply(event);
    }

    public void deleteTask() {
        final var data = TaskDeletedEvent.builder()
                .aggregateId(id)
                .boardId(boardId)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(TaskDeletedEvent.TASK_DELETED_V1, dataBytes);
        this.apply(event);
    }
}
