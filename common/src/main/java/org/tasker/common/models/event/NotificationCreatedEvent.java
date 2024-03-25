package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.NotificationAggregate;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class NotificationCreatedEvent extends BaseEvent {

    public static final String NOTIFICATION_CREATED_V1 = "NOTIFICATION_CREATED_V1";
    public static final String AGGREGATE_TYPE = NotificationAggregate.AGGREGATE_TYPE;

    @JsonProperty("user_id")
    private String userId;
    private String message;
    @JsonProperty("for_aggregate_type")
    private String forAggregateType;
    @JsonProperty("for_aggregate_id")
    private String forAggregateId;
    @JsonProperty("created_at")
    private Date createdAt;

    @Builder
    public NotificationCreatedEvent(@JsonProperty("aggregate_id") String aggregateId, String userId, String message, String forAggregateType, String forAggregateId, Date createdAt) {
        super(aggregateId);
        this.userId = userId;
        this.message = message;
        this.forAggregateType = forAggregateType;
        this.forAggregateId = forAggregateId;
        this.createdAt = createdAt;
    }
}

