package org.tasker.common.models.queries;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record GetTaskQuery(
        @JsonProperty("user_id")
        String userId,
        @JsonProperty("board_id")
        String taskId
) {
    public static final String QUERY_NAME = "get_task";
}
