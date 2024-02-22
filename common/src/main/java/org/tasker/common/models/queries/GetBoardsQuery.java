package org.tasker.common.models.queries;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record GetBoardsQuery(
        @JsonProperty("user_id")
        String userId
) {
    public static final String QUERY_NAME = "get_boards";
}
