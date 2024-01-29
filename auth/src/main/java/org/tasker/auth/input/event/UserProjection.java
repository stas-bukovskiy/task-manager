package org.tasker.auth.input.event;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.auth.mappers.UserMapper;
import org.tasker.auth.models.domain.UserAggregate;
import org.tasker.auth.models.domain.UserDocument;
import org.tasker.auth.models.events.UserCreatedEvent;
import org.tasker.auth.models.events.UserInfoUpdatedEvent;
import org.tasker.auth.output.persistance.UserRepository;
import org.tasker.common.es.Event;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.es.Projection;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.output.event.EventsMessagingSpecs;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProjection implements Projection {

    private final UserRepository userRepository;
    private final EventStoreDB eventStore;
    private final EventsMessagingSpecs messagingSpecs;
    private final Receiver receiver;
    private final Sender sender;

    private Map<String, Function<Event, Mono<Void>>> handlers;

    @PostConstruct
    public void init() {
        handlers = Map.of(
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
        );
    }

    @Override
    public Mono<String> declareQueue() {
        return sender.declareQueue(messagingSpecs.genEventQueueSpecs(UserAggregate.AGGREGATE_TYPE))
                .doOnNext(queue -> log.info("Queue <{}> declared", queue.getQueue()))
                .thenMany(Flux.fromIterable(messagingSpecs.genEvenBindSpecs(UserAggregate.AGGREGATE_TYPE, handlers.keySet().stream().toList())))
                .flatMap(sender::bind)
                .doOnNext(bindResult -> log.info("Bind result: {}", bindResult))
                .then(Mono.just(UserAggregate.AGGREGATE_TYPE));
    }

    @Override
    public void subscribeToQueue(String queueName) {
        receiver.consumeAutoAck(queueName)
                .subscribe(messageSpec -> {
                    log.info("Receive event to {}, with key {}", queueName, messageSpec.getEnvelope().getRoutingKey());

                    final Event event = SerializerUtils.deserializeFromJsonBytes(messageSpec.getBody(), Event.class);
                    processEvent(event).subscribe();
                });
    }

    private Mono<Void> processEvent(Event event) {
        if (!handlers.containsKey(event.getEventType())) {
            return Mono.empty();
        }

        final var eventHandler = handlers.get(event.getEventType());
        return eventHandler.apply(event)
                .onErrorResume(err -> {
                    log.error("error occurred while processed event {} for aggregateId {}", event.getEventType(), event.getAggregateId(), err);
                    return userRepository.deleteByAggregateId(event.getAggregateId())
                            .then(eventStore.load(event.getAggregateId(), UserAggregate.class))
                            .map(UserMapper::fromAggToDoc)
                            .flatMap(userRepository::insert)
                            .doOnSuccess(userDoc -> log.info("successfully restored user doc <{}> for aggregateId: {}", userDoc.getId(), userDoc.getAggregateId())).then();
                })
                .doOnSuccess(v -> log.info("event {} for aggregateId {} processed successfully", event.getEventType(), event.getAggregateId()));
    }
}
