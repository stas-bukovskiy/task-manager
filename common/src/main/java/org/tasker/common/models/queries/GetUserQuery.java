package org.tasker.common.models.queries;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record GetUserQuery(
        @JsonProperty("aggregate_id")
        String aggregateId,
        String search
) {
    public static final String QUERY_NAME = "get_user";
}
