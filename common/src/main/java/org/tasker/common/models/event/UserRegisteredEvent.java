package org.tasker.common.models.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.UserAggregate;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserRegisteredEvent extends BaseEvent {

    public static final String USER_REGISTERED_V1 = "user_registered_v1";
    public static final String AGGREGATE_TYPE = UserAggregate.AGGREGATE_TYPE;

    private String aggregateID;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

}