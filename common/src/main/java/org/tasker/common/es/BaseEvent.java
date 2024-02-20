package org.tasker.common.es;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public abstract class BaseEvent {

    @JsonProperty("aggregate_id")
    protected String aggregateId;

    public BaseEvent(String aggregateId) {
        Objects.requireNonNull(aggregateId);
        if (aggregateId.isBlank()) throw new RuntimeException("BaseEvent aggregateId is required");
        this.aggregateId = aggregateId;
    }

}