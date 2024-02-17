package org.tasker.common.models.domain;

import lombok.*;
import org.tasker.common.es.AggregateRoot;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.event.UserCreatedEvent;
import org.tasker.common.models.event.UserInfoUpdatedEvent;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
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

    @Override
    public void when(Event event) {
        switch (event.getEventType()) {
            case UserCreatedEvent.USER_CREATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), UserCreatedEvent.class));
            case UserInfoUpdatedEvent.USER_INFO_UPDATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), UserInfoUpdatedEvent.class));
        }
    }

    private void handle(UserCreatedEvent userCreatedEvent) {
        this.username = userCreatedEvent.getUsername();
        this.email = userCreatedEvent.getEmail();
        this.password = userCreatedEvent.getPassword();
        this.firstName = userCreatedEvent.getFirstName();
        this.lastName = userCreatedEvent.getLastName();
    }

    private void handle(UserInfoUpdatedEvent userInfoUpdatedEvent) {
        this.username = userInfoUpdatedEvent.getUsername();
        this.firstName = userInfoUpdatedEvent.getFirstName();
        this.lastName = userInfoUpdatedEvent.getLastName();
    }

    public void createUser(String username, String email, String password, String firstName, String lastName) {
        final var data = UserCreatedEvent.builder()
                .aggregateId(id)
                .email(email)
                .username(username)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(UserCreatedEvent.USER_CREATED_V1, dataBytes);
        this.apply(event);
    }

    public void updateUserInfo(String username, String firstName, String lastName) {
        final var data = UserInfoUpdatedEvent.builder()
                .aggregateId(id)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(UserInfoUpdatedEvent.USER_INFO_UPDATED_V1, dataBytes);
        this.apply(event);
    }
}
