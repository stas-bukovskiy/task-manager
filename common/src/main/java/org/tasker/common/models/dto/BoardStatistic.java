package org.tasker.common.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record BoardStatistic(
        @JsonProperty("created_num")
        int createdNum,
        @JsonProperty("joined_num")
        int joinedNum
) {
}
