package org.tasker.common.models.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.format.annotation.DateTimeFormat;
import org.tasker.common.models.enums.TaskPriority;
import org.tasker.common.models.enums.TaskStatus;

import java.util.Date;
import java.util.Set;

@Data
@Builder
@Document(collection = "tasks")
public class TaskDocument {

    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @Indexed(unique = true)
    @Field("aggregate_id")
    private String aggregateId;

    private String title;
    private String description;

    @Field("start_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date startDate;

    @Field("due_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dueDate;

    @Field("estimated_time")
    private int estimatedTime;
    private TaskPriority priority;
    private TaskStatus status;

    @Field("assignee_ids")
    private Set<String> assigneeIds;

    @Field("board_id")
    private String boardId;

    @Field("board")
    private BoardDocument board;
}
