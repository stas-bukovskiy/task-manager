package org.tasker.common.models.domain;

import lombok.*;
import org.tasker.common.es.AggregateRoot;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.event.UserRegisteredEvent;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserAggregate extends AggregateRoot {


    public static final String AGGREGATE_TYPE = "user_aggregate";
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    public UserAggregate(String id) {
        super(id, AGGREGATE_TYPE);
    }

    public void registerUser(String username, String email, String password, String firstName, String lastName) {
        final var data = UserRegisteredEvent.builder()
                .aggregateID(id)
                .username(username)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(UserRegisteredEvent.USER_REGISTERED_V1, dataBytes);
        this.apply(event);
    }

    @Override
    public void when(Event event) {

    }
}