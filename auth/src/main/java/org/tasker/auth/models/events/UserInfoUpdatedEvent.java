package org.tasker.auth.models.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.auth.models.domain.UserAggregate;
import org.tasker.common.es.BaseEvent;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserInfoUpdatedEvent extends BaseEvent {

    public static final String USER_INFO_UPDATED_V1 = "USER_INFO_UPDATED_V1";
    public static final String AGGREGATE_TYPE = UserAggregate.AGGREGATE_TYPE;

    private String username;
    private String firstName;
    private String lastName;

    @Builder
    public UserInfoUpdatedEvent(String aggregateId, String username, String firstName, String lastName) {
        super(aggregateId);
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

}
