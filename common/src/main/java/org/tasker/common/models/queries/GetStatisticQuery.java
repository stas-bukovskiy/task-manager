package org.tasker.common.models.queries;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record GetStatisticQuery(
        @JsonProperty("user_aggregate_id")
        String userAggregateId
) {
    public static final String QUERY_NAME = "get_statistic";
}
