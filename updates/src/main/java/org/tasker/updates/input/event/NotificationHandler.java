package org.tasker.updates.input.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.event.UserInvitedEvent;
import org.tasker.common.output.event.EventsMessagingSpecs;
import org.tasker.updates.models.response.NotificationResponse;
import org.tasker.updates.service.NotificationService;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

@Slf4j
@Component
public class NotificationHandler {

    private final Receiver receiver;
    private final String queueName;
    private final NotificationService notificationService;

    public NotificationHandler(Receiver receiver, Sender sender,
                               @Value("${communication.notification.queue}") String queueName,
                               EventsMessagingSpecs messagingSpecs,
                               NotificationService notificationService) {
        this.receiver = receiver;
        this.queueName = queueName;
        this.notificationService = notificationService;

        sender.declareQueue(QueueSpecification.queue(queueName).durable(true))
                .thenMany(Flux.just(messagingSpecs.toRoutingKey(UserInvitedEvent.AGGREGATE_TYPE, UserInvitedEvent.USER_INVITED_V1))
                        .flatMap(routingKey -> sender.bind(BindingSpecification.binding(messagingSpecs.getEventStoreExchange(), routingKey, queueName))))
                .subscribe();
    }


    public Flux<NotificationResponse<?>> subscribeToQueue() {
        return receiver.consumeAutoAck(queueName)
                .map(delivery -> SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), Event.class))
                .map(notificationService::processEvent);

    }

}