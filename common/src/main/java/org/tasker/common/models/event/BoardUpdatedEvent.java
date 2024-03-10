package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;

@Data
@EqualsAndHashCode(callSuper = true)
public class BoardUpdatedEvent extends BaseEvent {

    public static final String BOARD_UPDATED_V1 = "BOARD_UPDATED_V1";

    private String title;

    @Builder
    public BoardUpdatedEvent(@JsonProperty("aggregate_id") String aggregateId, String title) {
        super(aggregateId);
        this.title = title;
    }
}
