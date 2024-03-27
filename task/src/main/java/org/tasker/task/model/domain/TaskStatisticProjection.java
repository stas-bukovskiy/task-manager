package org.tasker.task.model.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class TaskStatisticProjection {
    @Field("to_do_num")
    private int toDoNum;
    @Field("in_progress_num")
    private int inProgressNum;
    @Field("in_revision_num")
    private int inRevisionNum;
    @Field("done_num")
    private int doneNum;
    @Field("archived_num")
    private int archivedNum;
}
