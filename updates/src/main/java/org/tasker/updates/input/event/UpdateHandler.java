package org.tasker.updates.input.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.event.BoardCreatedEvent;
import org.tasker.common.models.event.InvitationReviewedEvent;
import org.tasker.common.models.event.UserInvitedEvent;
import org.tasker.common.output.event.EventsMessagingSpecs;
import org.tasker.updates.models.response.UpdateResponse;
import org.tasker.updates.service.UpdateService;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

@Slf4j
@Component
public class UpdateHandler {

    private static final String[] ROUTING_KEYS = {
            UserInvitedEvent.AGGREGATE_TYPE + "." + UserInvitedEvent.USER_INVITED_V1,
            BoardCreatedEvent.AGGREGATE_TYPE + "." + BoardCreatedEvent.BOARD_CREATED_V1,
            InvitationReviewedEvent.AGGREGATE_TYPE + "." + InvitationReviewedEvent.INVITATION_REVIEWED_V1
    };

    private final Receiver receiver;
    private final String queueName;
    private final UpdateService updateService;

    public UpdateHandler(Receiver receiver, Sender sender,
                         @Value("${communication.notification.queue}") String queueName,
                         EventsMessagingSpecs messagingSpecs,
                         UpdateService updateService) {
        this.receiver = receiver;
        this.queueName = queueName;
        this.updateService = updateService;

        sender.declareQueue(QueueSpecification.queue(queueName).durable(true))
                .thenMany(Flux.fromArray(ROUTING_KEYS))
                .flatMap(routingKey -> sender.bind(BindingSpecification.binding(messagingSpecs.getEventStoreExchange(), routingKey, queueName)))
                .subscribe();
    }


    public Flux<UpdateResponse<?>> subscribeToQueue() {
        return receiver.consumeAutoAck(queueName)
                .map(delivery -> SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), Event.class))
                .map(updateService::processEvent)
                .flatMap(Flux::fromIterable);

    }

}