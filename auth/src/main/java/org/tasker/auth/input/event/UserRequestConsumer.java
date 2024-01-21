package org.tasker.auth.input.event;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.tasker.auth.exceptions.AlreadyExistsException;
import org.tasker.auth.exceptions.InvalidCredentialsException;
import org.tasker.auth.mappers.UserMapper;
import org.tasker.auth.service.AuthCommandService;
import org.tasker.auth.service.AuthQueryService;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.commands.RegisterNewUserCommand;
import org.tasker.common.models.commands.UpdateUserCommand;
import org.tasker.common.models.queries.GetUserQuery;
import org.tasker.common.models.queries.LoginUserQuery;
import org.tasker.common.models.queries.VerifyTokenQuery;
import org.tasker.common.models.response.DefaultResponse;
import org.tasker.common.models.response.UsersResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserRequestConsumer {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;
    private final Receiver receiver;
    private final Sender sender;
    private final AuthMessagingSpecs authMessagingSpecs;

    @PostConstruct
    public void init() {
        log.debug("Queue initializing...");

        var queue = authMessagingSpecs.requestExchangeSpec();
        var exchange = authMessagingSpecs.requestQueueSpec();
        sender.declareExchange(queue)
                .then(sender.declareQueue(exchange))
                .thenMany(Flux.just(RegisterNewUserCommand.COMMAND_NAME, UpdateUserCommand.COMMAND_NAME,
                                VerifyTokenQuery.QUERY_NAME, LoginUserQuery.QUERY_NAME, GetUserQuery.QUERY_NAME)
                        .map(authMessagingSpecs::requestBindingSpecs)
                        .flatMap(sender::bind))
                .doOnError(ex -> log.error("Error while initializing command queue: {}", ex.getMessage()))
                .subscribe();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void subscribeToQueue() {
        log.info("Subscribing to queue: {}", authMessagingSpecs.getRequestQueueName());

        receiver.consumeAutoAck(authMessagingSpecs.getRequestQueueName())
                .subscribe(delivery -> {
                    log.info("Received message on {}: {}", authMessagingSpecs.getRequestQueueName(), new String(delivery.getBody()));
                    final String commandName = delivery.getEnvelope().getRoutingKey();

                    switch (commandName) {
                        case RegisterNewUserCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), RegisterNewUserCommand.class), delivery.getProperties().getReplyTo());
                        case UpdateUserCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), UpdateUserCommand.class), delivery.getProperties().getReplyTo());
                        case VerifyTokenQuery.QUERY_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), VerifyTokenQuery.class), delivery.getProperties().getReplyTo());
                        case LoginUserQuery.QUERY_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), LoginUserQuery.class), delivery.getProperties().getReplyTo());
                        case GetUserQuery.QUERY_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), GetUserQuery.class), delivery.getProperties().getReplyTo());
                        default -> log.error("Unknown command: {}", commandName);
                    }
                });
    }

    private void handle(RegisterNewUserCommand command, String routingKey) {
        authCommandService.handle(command)
                .then(Mono.just(DefaultResponse.builder()
                        .httpCode(201)
                        .build()))
                .onErrorResume(ex -> {
                    if (ex instanceof AlreadyExistsException) {
                        return Mono.just(DefaultResponse.builder()
                                .httpCode(409)
                                .message(ex.getMessage())
                                .build());
                    } else {
                        log.error("Error while handling command: {}", ex.getMessage());
                        return Mono.just(DefaultResponse.builder()
                                .httpCode(500)
                                .message("Internal server error")
                                .build());
                    }
                })
                .subscribe(response -> {
                    log.info("Sending response on {}: {}", routingKey, response);

                    OutboundMessage message = new OutboundMessage(authMessagingSpecs.getResponseExchangeName(), routingKey, SerializerUtils.serializeToJsonBytes(response));
                    sender.send(Mono.just(message)).subscribe();
                });
    }

    private void handle(UpdateUserCommand command, String routingKey) {
        authCommandService.handle(command)
                .then(Mono.just(DefaultResponse.builder()
                        .httpCode(200)
                        .build()))
                .onErrorResume(ex -> {
                    if (ex instanceof AlreadyExistsException) {
                        return Mono.just(DefaultResponse.builder()
                                .httpCode(409)
                                .message(ex.getMessage())
                                .build());
                    } else {
                        log.error("Error while handling query", ex);
                        return Mono.just(DefaultResponse.builder()
                                .httpCode(500)
                                .message("Internal server error")
                                .build());
                    }
                })
                .subscribe(response -> {
                    log.info("Sending response on {}: {}", routingKey, response);

                    OutboundMessage message = new OutboundMessage(authMessagingSpecs.getResponseExchangeName(), routingKey, SerializerUtils.serializeToJsonBytes(response));
                    sender.send(Mono.just(message)).subscribe();
                });
    }

    private void handle(VerifyTokenQuery query, String routingKey) {
        authQueryService.handle(query)
                .map(aggregateID -> DefaultResponse.builder()
                        .httpCode(200)
                        .data(aggregateID)
                        .build())
                .onErrorResume(ex -> {
                    if (ex instanceof InvalidCredentialsException) {
                        return Mono.just(DefaultResponse.builder()
                                .httpCode(401)
                                .message(ex.getMessage())
                                .build());
                    } else {
                        log.error("Error while handling query", ex);
                        return Mono.just(DefaultResponse.builder()
                                .httpCode(500)
                                .message("Internal server error")
                                .build());
                    }
                })
                .subscribe(response -> {
                    log.info("Sending response on {}: {}", routingKey, response);

                    OutboundMessage message = new OutboundMessage(authMessagingSpecs.getResponseExchangeName(), routingKey, SerializerUtils.serializeToJsonBytes(response));
                    sender.send(Mono.just(message)).subscribe();
                });
    }

    private void handle(LoginUserQuery query, String routingKey) {
        authQueryService.handle(query)
                .map(token -> DefaultResponse.builder()
                        .data(token)
                        .httpCode(200)
                        .build())
                .onErrorResume(ex -> {
                    if (ex instanceof InvalidCredentialsException) {
                        return Mono.just(DefaultResponse.builder()
                                .httpCode(401)
                                .message(ex.getMessage())
                                .build());
                    } else {
                        log.error("Error while handling query", ex);
                        return Mono.just(DefaultResponse.builder()
                                .httpCode(500)
                                .message("Internal server error")
                                .build());
                    }
                })
                .subscribe(response -> {
                    log.info("Sending response on {}: {}", routingKey, response);

                    OutboundMessage message = new OutboundMessage(authMessagingSpecs.getResponseExchangeName(), routingKey, SerializerUtils.serializeToJsonBytes(response));
                    sender.send(Mono.just(message)).subscribe();
                });
    }

    private void handle(GetUserQuery query, String routingKey) {
        authQueryService.handle(query)
                .map(users -> UsersResponse.builder()
                        .httpCode(HttpStatus.OK.value())
                        .data(users.stream()
                                .map(UserMapper::fromDocToDto)
                                .toList())
                        .build())
                .onErrorResume(ex -> {
                            log.error("Error while handling query", ex);
                            return Mono.just(UsersResponse.builder()
                                    .httpCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .message("Internal server error")
                                    .build());
                        }
                )
                .subscribe(response -> {
                    log.info("Sending response on {}: {}", routingKey, response);

                    OutboundMessage message = new OutboundMessage(authMessagingSpecs.getResponseExchangeName(), routingKey, SerializerUtils.serializeToJsonBytes(response));
                    sender.send(Mono.just(message)).subscribe();
                });
    }
}