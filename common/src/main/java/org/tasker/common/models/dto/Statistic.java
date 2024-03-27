package org.tasker.common.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record Statistic(
        @JsonProperty("task_statistic")
        TaskStatistic taskStatistic
) {
}
