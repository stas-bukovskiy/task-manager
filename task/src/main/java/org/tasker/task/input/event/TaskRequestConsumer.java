package org.tasker.task.input.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.commands.CreateBoardCommand;
import org.tasker.common.models.commands.ReviewInvitationCommand;
import org.tasker.common.models.dto.Statistic;
import org.tasker.common.models.queries.GetBoardQuery;
import org.tasker.common.models.queries.GetBoardsQuery;
import org.tasker.common.models.queries.GetStatisticQuery;
import org.tasker.common.models.response.GetBoardsResponse;
import org.tasker.common.models.response.GetStatisticResponse;
import org.tasker.task.exception.BoardNotFoundException;
import org.tasker.task.service.BoardService;
import org.tasker.task.service.InvitationService;
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
    private final InvitationService invitationService;

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
                        case CreateBoardCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), CreateBoardCommand.class));
                        case ReviewInvitationCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), ReviewInvitationCommand.class));
                        case GetBoardsQuery.QUERY_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), GetBoardsQuery.class), delivery.getProperties().getReplyTo());
                        case GetBoardQuery.QUERY_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), GetBoardQuery.class), delivery.getProperties().getReplyTo());
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

    private void handle(CreateBoardCommand command) {
        boardService.createBoard(command)
                .doOnError(ex -> log.error("Failed to create board: {}", command, ex))
                .doOnSuccess(v -> log.info("Created new board '{}' for user <{}>", command.title(), command.ownerId()))
                .subscribe();
    }

    private void handle(ReviewInvitationCommand command) {
        invitationService.reviewInvitation(command)
                .doOnError(ex -> log.error("Failed to review invitation: {}", command, ex))
                .doOnSuccess(v -> log.info("Reviewed invitation <{}> for user <{}>", command.invitationId(), command.userId()))
                .subscribe();
    }

    private void handle(GetBoardsQuery query, String routingKey) {
        boardService.getBoards(query.userId())
                .map(boards -> GetBoardsResponse.builder()
                        .httpCode(HttpStatus.OK.value())
                        .data(boards)
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

    private void handle(GetBoardQuery query, String routingKey) {
        boardService.getBoard(query.userId(), query.boardId())
                .map(board -> GetStatisticResponse.builder()
                        .httpCode(HttpStatus.OK.value())
                        .data(board)
                        .build())
                .onErrorResume(ex -> {
                            if (ex instanceof BoardNotFoundException) {
                                return Mono.just(GetBoardsResponse.builder()
                                        .httpCode(HttpStatus.NOT_FOUND.value())
                                        .message(ex.getMessage())
                                        .build());
                            }

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