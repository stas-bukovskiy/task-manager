package org.tasker.task.input.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.dto.Statistic;
import org.tasker.common.models.queries.GetStatisticQuery;
import org.tasker.common.models.response.GetStatisticResponse;
import org.tasker.task.service.BoardService;
import org.tasker.task.service.TaskService;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

@Slf4j
@RequiredArgsConstructor
@Component
public class TaskRequestConsumer {

    private final Receiver receiver;
    private final Sender sender;
    private final TaskMessagingSpecs taskMessagingSpecs;
    private final TaskService taskService;
    private final BoardService boardService;

    @EventListener(ApplicationReadyEvent.class)
    public void subscribeToQueue() {
        log.info("Subscribing to queue: {}", taskMessagingSpecs.getRequestQueueName());

        receiver.consumeAutoAck(taskMessagingSpecs.getRequestQueueName())
                .subscribe(delivery -> {
                    log.info("Received message on {}: {}", taskMessagingSpecs.getRequestQueueName(), new String(delivery.getBody()));
                    final String commandName = delivery.getEnvelope().getRoutingKey();

                    switch (commandName) {
                        case GetStatisticQuery.QUERY_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), GetStatisticQuery.class), delivery.getProperties().getReplyTo());
                        default -> log.error("Unknown command: {}", commandName);
                    }
                });
    }

    private void handle(GetStatisticQuery query, String routingKey) {
        taskService.getStatistic(query.userAggregateId())
                .zipWith(boardService.getStatistic(query.userAggregateId()))
                .map(statistics -> GetStatisticResponse.builder()
                        .httpCode(HttpStatus.OK.value())
                        .data(Statistic.builder()
                                .taskStatistic(statistics.getT1())
                                .boardStatistic(statistics.getT2())
                                .build())
                        .build())
                .onErrorResume(ex -> {
                            log.error("Error while handling query", ex);
                            return Mono.just(GetStatisticResponse.builder()
                                    .httpCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .message("Internal server error")
                                    .build());
                        }
                )
                .subscribe(response -> {
                    log.info("Sending response on {}: {}", routingKey, response);

                    OutboundMessage message = new OutboundMessage(taskMessagingSpecs.getResponseExchangeName(), routingKey, SerializerUtils.serializeToJsonBytes(response));
                    sender.send(Mono.just(message)).subscribe();
                });
    }

}