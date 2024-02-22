package org.tasker.auth.models.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.UserAggregate;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserInfoUpdatedEvent extends BaseEvent {

    public static final String USER_INFO_UPDATED_V1 = "USER_INFO_UPDATED_V1";
    public static final String AGGREGATE_TYPE = UserAggregate.AGGREGATE_TYPE;

    private String username;
    private String firstName;
    private String lastName;

    @Builder
    public UserInfoUpdatedEvent(@JsonProperty("aggregate_id") String aggregateId, String username,
                                String firstName, String lastName) {
        super(aggregateId);
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

}

