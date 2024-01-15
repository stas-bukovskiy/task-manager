package org.tasker.auth.mappers;

import org.tasker.auth.models.domain.UserAggregate;
import org.tasker.auth.models.domain.UserDocument;
import org.tasker.common.es.Event;

import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }


    public static UserDocument fromAggToDoc(UserAggregate agg) {
        return UserDocument.builder()
                .aggregateId(agg.getId())
                .username(agg.getUsername())
                .email(agg.getEmail())
                .password(agg.getPassword())
                .firstName(agg.getFirstName())
                .lastName(agg.getLastName())
                .processedEvents(agg.getChanges().stream()
                        .map(Event::getAggregateId)
                        .collect(Collectors.toSet()))
                .build();
    }

}