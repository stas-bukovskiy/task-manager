package org.tasker.task.mapper;

import org.tasker.common.models.domain.TaskAggregate;
import org.tasker.common.models.domain.TaskDocument;
import org.tasker.common.models.dto.TaskDto;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public final class TaskMapper {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private TaskMapper() {
    }

    public static TaskDocument fromAggToDoc(TaskAggregate agg) {
        return TaskDocument.builder()
                .aggregateId(agg.getId())
                .title(agg.getTitle())
                .description(agg.getDescription())
                .startDate(agg.getStartDate())
                .dueDate(agg.getDueDate())
                .estimatedTime(agg.getEstimatedTime())
                .priority(agg.getPriority())
                .status(agg.getStatus())
                .assigneeIds(agg.getAssigneeIds())
                .build();
    }


    public static TaskDto fromDocToDTO(TaskDocument taskDocument) {
        return TaskDto.builder()
                .aggregateId(taskDocument.getAggregateId())
                .title(taskDocument.getTitle())
                .description(taskDocument.getDescription())
                .startDate(dateFormat.format(taskDocument.getStartDate()))
                .dueDate(dateFormat.format(taskDocument.getDueDate()))
                .estimatedTime(taskDocument.getEstimatedTime())
                .priority(taskDocument.getPriority())
                .status(taskDocument.getStatus())
                .assigneeIds(taskDocument.getAssigneeIds())
                .boardId(taskDocument.getBoardId())
                .build();
    }
}
