package org.tasker.auth.input.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.auth.mappers.UserMapper;
import org.tasker.auth.models.domain.UserDocument;
import org.tasker.auth.models.events.UserCreatedEvent;
import org.tasker.auth.models.events.UserInfoUpdatedEvent;
import org.tasker.auth.output.persistance.UserRepository;
import org.tasker.common.es.Event;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.es.Projection;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.domain.UserAggregate;
import org.tasker.common.output.event.EventsMessagingSpecs;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class UserProjection extends Projection {

    private static final String[] ROUTING_KEYS = new String[]{UserAggregate.AGGREGATE_TYPE + ".*"};
    private final UserRepository userRepository;
    private final EventStoreDB eventStore;

    public UserProjection(UserRepository userRepository, EventStoreDB eventStore, EventsMessagingSpecs messagingSpecs, Receiver receiver) {
        super(receiver, messagingSpecs, Map.of(
                UserCreatedEvent.USER_CREATED_V1, (event ->
                        userRepository.findByAggregateId(event.getAggregateId())
                                .doOnNext(existed -> log.info("user doc <{}> already exist with such aggregateId: {}", existed.getId(), existed.getAggregateId()))
                                .switchIfEmpty(Mono.fromCallable(() -> {
                                            final var baseEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), UserCreatedEvent.class);
                                            return UserDocument.builder()
                                                    .aggregateId(baseEvent.getAggregateId())
                                                    .email(baseEvent.getEmail())
                                                    .username(baseEvent.getUsername())
                                                    .password(baseEvent.getPassword())
                                                    .firstName(baseEvent.getFirstName())
                                                    .lastName(baseEvent.getLastName())
                                                    .processedEvents(Set.of(event.getId().toString()))
                                                    .build();
                                        }).flatMap(userRepository::insert)
                                        .doOnNext(inserted -> log.info("user doc <{}> created for aggregateId: {}", inserted.getId(), inserted.getAggregateId())))
                                .then()),
                UserInfoUpdatedEvent.USER_INFO_UPDATED_V1, (event ->
                        userRepository.findByAggregateId(event.getAggregateId())
                                .<UserDocument>handle((userDoc, sink) -> {
                                    if (userDoc.getProcessedEvents().contains(event.getId().toString())) {
                                        sink.complete();
                                        return;
                                    }

                                    final var baseEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), UserInfoUpdatedEvent.class);
                                    userDoc.setUsername(baseEvent.getUsername());
                                    userDoc.setFirstName(baseEvent.getFirstName());
                                    userDoc.setLastName(baseEvent.getLastName());

                                    var processedEvents = new HashSet<>(userDoc.getProcessedEvents());
                                    processedEvents.add(event.getId().toString());
                                    userDoc.setProcessedEvents(processedEvents);

                                    sink.next(userDoc);
                                })
                                .flatMap(userRepository::save)
                                .then()
                )
        ), UserAggregate.AGGREGATE_TYPE, ROUTING_KEYS);
        this.userRepository = userRepository;
        this.eventStore = eventStore;
    }

    @Override
    protected Mono<Void> handleError(Throwable err, Event event) {
        return userRepository.deleteByAggregateId(event.getAggregateId())
                .then(eventStore.load(event.getAggregateId(), UserAggregate.class))
                .map(UserMapper::fromAggToDoc)
                .flatMap(userRepository::insert)
                .doOnSuccess(userDoc -> log.info("successfully restored user doc <{}> for aggregateId: {}", userDoc.getId(), userDoc.getAggregateId())).then();
    }

}
