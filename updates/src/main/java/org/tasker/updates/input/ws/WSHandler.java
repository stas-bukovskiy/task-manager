
package org.tasker.updates.input.ws;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.server.ResponseStatusException;
import org.tasker.common.es.SerializerUtils;
import org.tasker.updates.input.event.UpdateHandler;
import org.tasker.updates.models.request.*;
import org.tasker.updates.models.response.ErrorMessages;
import org.tasker.updates.models.response.ErrorResponse;
import org.tasker.updates.models.response.WSResponse;
import org.tasker.updates.service.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Slf4j
@Component
public class WSHandler implements WebSocketHandler {

    private final UpdateHandler updateHandler;
    private final UserService userService;
    private final ValidationService validator;
    private final WSRequestDeserializeService wsRequestDeserializeService;
    private final TaskService taskService;
    private final InvitationService invitationService;
    private final ConcurrentMap<String, Set<WebSocketSession>> activeSessions;

    public WSHandler(UserService userService, ValidationService validator, WSRequestDeserializeService wsRequestDeserializeService,
                     @Qualifier("updatesTaskService") TaskService taskService, UpdateHandler updateHandler,
                     @Qualifier("updatesInvitationService") InvitationService invitationService) {
        this.userService = userService;
        this.validator = validator;
        this.wsRequestDeserializeService = wsRequestDeserializeService;
        this.taskService = taskService;
        this.updateHandler = updateHandler;
        this.invitationService = invitationService;

        activeSessions = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        updateHandler.subscribeToQueue()
                .doOnError(ex -> log.error("Error while subscribing to notification queues", ex))
                .subscribe(notification -> {
                    log.info("Notification sent: {}", notification);
                    activeSessions.getOrDefault(notification.toUserId(), Set.of()).forEach(session -> {
                        if (session.isOpen()) {
                            session.send(Mono.just(session.textMessage(SerializerUtils.serializeToJsonString(
                                    notification
                            )))).subscribe();
                        } else {
                            activeSessions.get(notification.toUserId()).remove(session);
                            log.info("Session {} is closed for user {}", session.getId(), notification.toUserId());
                        }
                    });
                });
    }

    @Override
    // TODO: add timeout
    public Mono<Void> handle(WebSocketSession session) {
        return Flux.merge(
                Flux.deferContextual(ctx -> {
                    final String currentUserId = ctx.get("aggregate_id");
                    activeSessions.putIfAbsent(currentUserId, ConcurrentHashMap.newKeySet());
                    activeSessions.get(currentUserId).add(session);
                    return Mono.just(currentUserId);
                }).doOnNext(currentUserId -> log.info("New session: {}, for user {}", session.getId(), currentUserId)),
                session.receive()
                        .<WSRequest>handle((requestRaw, sink) -> {
                            var payload = requestRaw.getPayload();
                            var payloadBytes = new byte[payload.readableByteCount()];
                            payload.read(payloadBytes);

                            try {
                                sink.next(wsRequestDeserializeService.deserialize(payloadBytes));
                            } catch (Exception ex) {
                                sink.error(ex);
                            }
                        })
                        .flatMap(wsRequest -> delegateRequest(session, wsRequest)
                                .onErrorResume(ex -> {
                                    log.info("Error while handling WS request: {}", wsRequest, ex);
                                    return sendErrorResponse(session, wsRequest.correlationId(), ex);
                                }))
                        .onErrorResume(ex -> {
                            if (ex instanceof ResponseStatusException responseStatusException) {
                                if (responseStatusException.getStatusCode().is4xxClientError()) {
                                    log.info("Error while handling WS request: {}", responseStatusException.getReason());
                                } else {
                                    log.error("Error while handling WS request", ex);
                                }
                            }
                            return Mono.empty();
                        })
                        .thenMany(Flux.empty())
        ).then();
    }

    private Mono<Void> delegateRequest(WebSocketSession session, WSRequest wsRequest) {
        switch (wsRequest.type()) {
            case "get_me" -> {
                return Mono.deferContextual(ctx -> {
                            final String currentUserId = ctx.get("aggregate_id");
                            return userService.getUserByAggregateId(currentUserId);
                        })
                        .flatMap(userBytes -> sendResponse(session, wsRequest, userBytes));
            }
            case "update_user_info" -> {
                return Mono.deferContextual(ctx -> {
                            if (wsRequest.data() == null) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                            }
                            final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), UpdateUserInfoRequest.class);
                            validator.validate(request, "update_user_info_request");

                            final String currentUserId = ctx.get("aggregate_id");
                            return userService.updateUserInfo(currentUserId, request);
                        })
                        .flatMap((response) -> {
                            if (response.getHttpCode() != HttpStatus.OK.value()) {
                                return Mono.error(new ResponseStatusException(HttpStatusCode.valueOf(response.getHttpCode()), response.getMessage()));
                            }
                            return sendResponse(session, wsRequest, response);
                        })
                        .onErrorResume(ex -> {
                            log.error("Error while updating user info", ex);
                            return sendErrorResponse(session, wsRequest.correlationId(), ex);
                        });
            }
            case "get_user_statistic" -> {
                return Mono.deferContextual(ctx -> {
                            final String currentUserId = ctx.get("aggregate_id");
                            return taskService.getUserStatistic(currentUserId);
                        })
                        .flatMap(userBytes -> sendResponse(session, wsRequest, userBytes));
            }
            case "search_people" -> {
                return Mono.fromCallable(() -> {
                            if (wsRequest.data() == null) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                            }
                            final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), SearchPeopleRequest.class);
                            validator.validate(request, "search_people_request");

                            return userService.searchPeople(request.search());
                        })
                        .flatMap(Function.identity())
                        .flatMap(users -> sendResponse(session, wsRequest, users))
                        .onErrorResume(ex -> {
                            log.error("Error while updating user info", ex);
                            return sendErrorResponse(session, wsRequest.correlationId(), ex);
                        });
            }
            case "create_board" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }
                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), CreateBoardRequest.class);
                    validator.validate(request, "create_board_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return taskService.createBoard(currentUserId, request);
                });
            }
            case "review_invitation" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), ReviewInitiationRequest.class);
                    validator.validate(request, "review_invitation_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return invitationService.reviewInvitation(currentUserId, request);
                });
            }
            case "get_boards" -> {
                return Mono.deferContextual(ctx -> {
                            final String currentUserId = ctx.get("aggregate_id");
                            return taskService.getBoards(currentUserId);
                        }).flatMap((response) -> {
                            if (response.getHttpCode() != HttpStatus.OK.value()) {
                                return Mono.error(new ResponseStatusException(HttpStatusCode.valueOf(response.getHttpCode()), response.getMessage()));
                            }
                            return sendResponse(session, wsRequest, response);
                        })
                        .onErrorResume(ex -> {
                            log.error("Error while updating user info", ex);
                            return sendErrorResponse(session, wsRequest.correlationId(), ex);
                        });
            }
            case "get_board" -> {
                return Mono.deferContextual(ctx -> {
                            if (wsRequest.data() == null) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                            }

                            final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), GetBoardRequest.class);
                            validator.validate(request, "get_board_request");

                            final String currentUserId = ctx.get("aggregate_id");
                            return taskService.getBoard(currentUserId, request.boardId());
                        }).flatMap((response) -> {
                            if (response.getHttpCode() != HttpStatus.OK.value()) {
                                return Mono.error(new ResponseStatusException(HttpStatusCode.valueOf(response.getHttpCode()), response.getMessage()));
                            }
                            return sendResponse(session, wsRequest, response);
                        })
                        .onErrorResume(ex -> {
                            log.error("Error while updating user info", ex);
                            return sendErrorResponse(session, wsRequest.correlationId(), ex);
                        });
            }
            default -> {
                log.error("Invalid request type: {}", wsRequest.type());
                return Mono.empty();
            }
        }
    }

    private <T> Mono<Void> sendResponse(WebSocketSession session, WSRequest wsRequest, T data) {
        return session.send(Mono.just(session.textMessage(SerializerUtils.serializeToJsonString(
                new WSResponse<>(wsRequest.correlationId(), data)
        ))));
    }

    private Mono<Void> sendErrorResponse(WebSocketSession session, String correlationId, Throwable ex) {
        ErrorResponse errorResponse;
        if (ex instanceof ResponseStatusException) {
            errorResponse = ErrorResponse.builder()
                    .status(((ResponseStatusException) ex).getStatusCode().value())
                    .message(((ResponseStatusException) ex).getReason())
                    .timestamp(System.currentTimeMillis())
                    .correlationId(correlationId)
                    .build();
        } else {
            errorResponse = ErrorResponse.builder()
                    .status(500)
                    .message(ErrorMessages.INTERNAL_SERVER_ERROR)
                    .timestamp(System.currentTimeMillis())
                    .correlationId(correlationId)
                    .build();
        }

        return session.send(Mono.just(session.textMessage(SerializerUtils.serializeToJsonString(errorResponse))));
    }

}