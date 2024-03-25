package org.tasker.notification.input.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.Event;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.es.Projection;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.domain.NotificationAggregate;
import org.tasker.common.models.event.NotificationCreatedEvent;
import org.tasker.common.models.event.NotificationInvalidatedEvent;
import org.tasker.common.output.event.EventsMessagingSpecs;
import org.tasker.notification.mapper.NotificationMapper;
import org.tasker.notification.models.domain.NotificationDocument;
import org.tasker.notification.output.persistance.NotificationRepository;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Service
public class NotificationProjection extends Projection {

    private final static String[] ROUTING_KEYS = new String[]{
            NotificationAggregate.AGGREGATE_TYPE + ".*",
    };

    private final NotificationRepository notificationRepository;
    private final EventStoreDB eventStore;


    public NotificationProjection(NotificationRepository notificationRepository, EventStoreDB eventStore, EventsMessagingSpecs messagingSpecs,
                                  Receiver receiver) {
        super(receiver, messagingSpecs, Map.of(
                NotificationCreatedEvent.NOTIFICATION_CREATED_V1, (event ->
                        notificationRepository.existsByAggregateId(event.getAggregateId())
                                .handle((exists, sink) -> {
                                    if (exists) {
                                        log.info("notification doc <{}> already exist with such aggregateId: {}", event.getId(), event.getAggregateId());
                                        sink.complete();
                                    } else {
                                        sink.next(false);
                                    }
                                })
                                .map(ignored -> {
                                    final var notificationCreatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), NotificationCreatedEvent.class);
                                    return NotificationDocument.builder()
                                            .aggregateId(event.getAggregateId())
                                            .userId(notificationCreatedEvent.getUserId())
                                            .forAggregateType(notificationCreatedEvent.getForAggregateType())
                                            .forAggregateId(notificationCreatedEvent.getForAggregateId())
                                            .message(notificationCreatedEvent.getMessage())
                                            .valid(true)
                                            .deleted(false)
                                            .createdAt(notificationCreatedEvent.getCreatedAt())
                                            .build();
                                }).flatMap(notificationRepository::insert)
                                .doOnNext(inserted -> log.info("notification info doc <{}> created for aggregateId: {}", inserted.getId(), inserted.getAggregateId()))
                                .then()),
                NotificationInvalidatedEvent.NOTIFICATION_INVALIDATED_V1, (event ->
                        getByAggregateId(notificationRepository, event.getAggregateId())
                                .map(notificationDoc -> {
                                    notificationDoc.setValid(false);
                                    return notificationDoc;
                                })
                                .flatMap(notificationRepository::save)
                                .doOnNext(updated -> log.info("notification info doc <{}> invalidated for aggregateId: {}", updated.getId(), updated.getAggregateId()))
                                .then())
        ), NotificationAggregate.AGGREGATE_TYPE, ROUTING_KEYS);

        this.notificationRepository = notificationRepository;
        this.eventStore = eventStore;
    }

    private static Mono<NotificationDocument> getByAggregateId(NotificationRepository notificationRepository, String aggregateId) {
        return notificationRepository.findByAggregateId(aggregateId)
                .repeatWhenEmpty(3, (retrySpec) -> retrySpec.delayElements(Duration.of(1, ChronoUnit.SECONDS)));
    }

    @Override
    protected Mono<Void> handleError(Throwable err, Event event) {
        return notificationRepository.deleteByAggregateId(event.getAggregateId())
                .then(eventStore.load(event.getAggregateId(), NotificationAggregate.class))
                .map(NotificationMapper::fromAggToDoc)
                .flatMap(notificationRepository::insert)
                .doOnSuccess(notificationDoc -> log.info("successfully restored notification doc <{}> for aggregateId: {}", notificationDoc.getId(), notificationDoc.getAggregateId()))
                .then();
    }

}
