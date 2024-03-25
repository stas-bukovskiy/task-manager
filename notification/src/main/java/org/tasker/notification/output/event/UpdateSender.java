package org.tasker.notification.output.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.dto.UpdateDto;
import org.tasker.notification.input.event.NotificationMessagingSpecs;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@Service
@RequiredArgsConstructor
public class UpdateSender {

    private final Sender sender;
    private final NotificationMessagingSpecs messagingSpecs;

    public Mono<Void> sendUpdate(UpdateDto<?> updateDto) {
        OutboundMessage message = new OutboundMessage(messagingSpecs.getUpdatesExchangeName(), messagingSpecs.getUpdatesExchangeName(), SerializerUtils.serializeToJsonBytes(updateDto));
        return sender.send(Mono.just(message));
    }
}
