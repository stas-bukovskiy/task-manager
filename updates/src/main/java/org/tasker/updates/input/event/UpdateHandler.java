package org.tasker.updates.input.event;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.dto.UpdateDto;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.Receiver;

@Service
public class UpdateHandler {

    private final Receiver receiver;
    private final String queueName;

    public UpdateHandler(Receiver receiver, @Value("${communication.notification.updates-queue}") String queueName) {
        this.receiver = receiver;
        this.queueName = queueName;
    }

    public Flux<UpdateDto<?>> subscribeToNotifications() {
        return receiver.consumeAutoAck(queueName)
                .map(delivery -> SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), new TypeReference<>() {
                }));
    }
}
