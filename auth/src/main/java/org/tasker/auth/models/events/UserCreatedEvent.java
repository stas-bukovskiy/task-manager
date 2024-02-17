package org.tasker.auth.models.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.UserAggregate;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserCreatedEvent extends BaseEvent {

    public static final String USER_CREATED_V1 = "USER_CREATED_V1";
    public static final String AGGREGATE_TYPE = UserAggregate.AGGREGATE_TYPE;

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    @Builder
    public UserCreatedEvent(String aggregateId, String username, String email, String password, String firstName, String lastName) {
        super(aggregateId);
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

}

