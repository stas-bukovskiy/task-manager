package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.BoardAggregate;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BoardCreatedEvent extends BaseEvent {

    public static final String BOARD_CREATED_V1 = "BOARD_CREATED_V1";
    public static final String AGGREGATE_TYPE = BoardAggregate.AGGREGATE_TYPE;

    private String title;
    @JsonProperty("owner_id")
    private String ownerId;
    @JsonProperty("invited_ids")
    private List<String> invitedIds;

    @Builder
    public BoardCreatedEvent(String aggregateId, String title, String ownerId, List<String> invitedIds) {
        super(aggregateId);
        this.title = title;
        this.ownerId = ownerId;
        this.invitedIds = invitedIds;
    }
}

