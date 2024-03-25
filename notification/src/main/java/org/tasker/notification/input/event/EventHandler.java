package org.tasker.notification.input.event;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.dto.UpdateDto;
import org.tasker.common.models.event.*;
import org.tasker.common.output.event.EventsMessagingSpecs;
import org.tasker.notification.output.event.UpdateSender;
import org.tasker.notification.service.NotificationService;
import org.tasker.notification.service.UpdateService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

@Slf4j
@Component
public class EventHandler {

    private static final String[] ROUTING_KEYS = {
            BoardCreatedEvent.AGGREGATE_TYPE + "." + BoardCreatedEvent.BOARD_CREATED_V1,
            BoardDeletedEvent.AGGREGATE_TYPE + "." + BoardDeletedEvent.BOARD_DELETED_V1,
            BoardMemberDeletedEvent.AGGREGATE_TYPE + "." + BoardMemberDeletedEvent.BOARD_MEMBER_DELETED_V1,
            BoardUpdatedEvent.AGGREGATE_TYPE + "." + BoardUpdatedEvent.BOARD_UPDATED_V1,
            UserInvitedEvent.AGGREGATE_TYPE + "." + UserInvitedEvent.USER_INVITED_V1,
            InvitationDeletedEvent.AGGREGATE_TYPE + "." + InvitationDeletedEvent.INVITATION_DELETED_V1,
            InvitationReviewedEvent.AGGREGATE_TYPE + "." + InvitationReviewedEvent.INVITATION_REVIEWED_V1,
            TaskAssigneeAdded.AGGREGATE_TYPE + "." + TaskAssigneeAdded.TASK_ASSIGNEE_ADDED_V1,
            TaskAssigneeDeleted.AGGREGATE_TYPE + "." + TaskAssigneeDeleted.TASK_ASSIGNEE_DELETED_V1,
            TaskCreatedEvent.AGGREGATE_TYPE + "." + TaskCreatedEvent.TASK_CREATED_V1,
            TaskDeletedEvent.AGGREGATE_TYPE + "." + TaskDeletedEvent.TASK_DELETED_V1,
            TaskInfoUpdatedEvent.AGGREGATE_TYPE + "." + TaskInfoUpdatedEvent.TASK_INFO_UPDATED_V1,
            TaskStatusUpdatedEvent.AGGREGATE_TYPE + "." + TaskStatusUpdatedEvent.TASK_STATUS_UPDATED_V1,
            NotificationCreatedEvent.AGGREGATE_TYPE + "." + NotificationCreatedEvent.NOTIFICATION_CREATED_V1,
            NotificationInvalidatedEvent.AGGREGATE_TYPE + "." + NotificationInvalidatedEvent.NOTIFICATION_INVALIDATED_V1
    };

    private final Receiver receiver;
    private final UpdateSender sender;
    private final String queueName;
    private final UpdateService updateService;
    private final NotificationService notificationService;

    public EventHandler(Receiver receiver, Sender sender,
                        @Value("${communication.notification.events-queue}") String queueName,
                        EventsMessagingSpecs messagingSpecs, UpdateSender sender1,
                        @Qualifier("notificationUpdateService") UpdateService updateService,
                        NotificationService notificationService) {
        this.receiver = receiver;
        this.queueName = queueName;
        this.sender = sender1;
        this.updateService = updateService;
        this.notificationService = notificationService;

        sender.declareQueue(QueueSpecification.queue(queueName).durable(true))
                .thenMany(Flux.fromArray(ROUTING_KEYS))
                .flatMap(routingKey -> sender.bind(BindingSpecification.binding(messagingSpecs.getEventStoreExchange(), routingKey, queueName)))
                .subscribe();
    }


    public Flux<UpdateDto<?>> subscribeToQueue() {
        return receiver.consumeAutoAck(queueName)
                .map(delivery -> SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), Event.class))
                .flatMap(event -> Flux.merge(
                        updateService.processEvent(event)
                                .onErrorResume(error -> {
                                    log.error("error occurred while processed event for updates", error);
                                    return Mono.empty();
                                }),
                        notificationService.processEvent(event)
                                .onErrorResume(error -> {
                                    log.error("error occurred while processed event for notification, event: {}", event.getEventType(), error);
                                    return Mono.empty();
                                })
                                .thenMany(Flux.empty())
                )).onErrorResume(err -> {
                    log.error("error occurred while processed event", err);
                    return Flux.empty();
                });
    }

    @PostConstruct
    public void init() {
        log.info("EventHandler is ready to process events");
        subscribeToQueue().
                flatMap(sender::sendUpdate)
                .subscribe();
    }

}