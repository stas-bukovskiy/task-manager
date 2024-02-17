package org.tasker.common.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record TaskStatistic(
        @JsonProperty("overall_task_statistic")
        TaskStatisticByStatus overallTaskStatistic,
        @JsonProperty("assigned_task_statistic")
        TaskStatisticByStatus assignedTaskStatistic

) {
    @Builder
    public record TaskStatisticByStatus(
            @JsonProperty("to_do_num")
            int toDoNum,
            @JsonProperty("in_progress_num")
            int inProgressNum,
            @JsonProperty("in_revision_num")
            int inRevisionNum,
            @JsonProperty("done_num")
            int doneNum,
            @JsonProperty("archived_num")
            int archivedNum
    ) {
    }
}
