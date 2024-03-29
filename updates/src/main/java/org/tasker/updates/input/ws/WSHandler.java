
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
import reactor.core.scheduler.Schedulers;

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
    private final UserStatusService userStatusService;
    private final ConcurrentMap<String, Set<WebSocketSession>> activeSessions;

    public WSHandler(UpdateHandler updateHandler, UserService userService, ValidationService validator, WSRequestDeserializeService wsRequestDeserializeService,
                     @Qualifier("updatesTaskService") TaskService taskService,
                     @Qualifier("updatesInvitationService") InvitationService invitationService,
                     @Qualifier("updatesUserStatusService") UserStatusService userStatusService) {
        this.updateHandler = updateHandler;
        this.userService = userService;
        this.validator = validator;
        this.wsRequestDeserializeService = wsRequestDeserializeService;
        this.taskService = taskService;
        this.invitationService = invitationService;
        this.userStatusService = userStatusService;

        activeSessions = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        updateHandler.subscribeToNotifications()
                .doOnError(ex -> log.error("Error while subscribing to update queue", ex))
                .subscribe(notification -> {
                    notification.toUserIds().forEach(userId -> {
                        activeSessions.getOrDefault(userId, Set.of()).forEach(session -> {
                            if (session.isOpen()) {
                                session.send(Mono.just(session.textMessage(SerializerUtils.serializeToJsonString(
                                        notification
                                )))).subscribe();
                            } else {
                                activeSessions.get(userId).remove(session);
                                userStatusService.updateUserStatus(userId, false)
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .subscribe();
                                log.info("Session {} is closed for user {}", session.getId(), userId);
                            }
                        });
                    });
                });
    }

    @Override
    // TODO: add timeout
    public Mono<Void> handle(WebSocketSession session) {
        final String userId = (String) session.getAttributes().get("aggregate_id");
        return Flux.merge(
                        Flux.deferContextual(ctx -> {
                                    final String currentUserId = ctx.get("aggregate_id");
                                    activeSessions.putIfAbsent(currentUserId, ConcurrentHashMap.newKeySet());
                                    activeSessions.get(currentUserId).add(session);
                                    return Mono.just(currentUserId);
                                }).flatMap(currentUserId -> userStatusService.updateUserStatus(currentUserId, true)
                                        .then(Mono.just(currentUserId)))
                                .doOnNext(currentUserId -> log.info("New session: {}, for user {}", session.getId(), currentUserId)),
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
                ).doFinally(ignored -> {
                    activeSessions.getOrDefault(userId, Set.of()).remove(session);
                    userStatusService.updateUserStatus(userId, false)
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe();
                    log.info("Session {} is closed for user {}", session.getId(), userId);
                })
                .then();
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
            case "update_board" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), UpdateBoardRequest.class);
                    validator.validate(request, "update_board_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return taskService.updateBoard(currentUserId, request);
                });
            }
            case "invite_users" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), InviteUsersRequest.class);
                    validator.validate(request, "invite_user_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return invitationService.inviteUser(currentUserId, request);
                });
            }
            case "delete_invitation" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), DeleteInvitationRequest.class);
                    validator.validate(request, "delete_invitation_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return invitationService.deleteInvitation(currentUserId, request);
                });
            }
            case "delete_member" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), DeleteMemberRequest.class);
                    validator.validate(request, "delete_member_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return taskService.deleteMember(currentUserId, request);
                });
            }
            case "delete_board" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), DeleteBoardRequest.class);
                    validator.validate(request, "delete_board_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return taskService.deleteBoard(currentUserId, request);
                });
            }
            case "get_tasks" -> {
                return Mono.deferContextual(ctx -> {
                            if (wsRequest.data() == null) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                            }

                            final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), GetTasksRequest.class);
                            validator.validate(request, "get_tasks_request");

                            final String currentUserId = ctx.get("aggregate_id");
                            return taskService.getTasks(currentUserId, request);
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
            case "get_task" -> {
                return Mono.deferContextual(ctx -> {
                            if (wsRequest.data() == null) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                            }

                            final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), GetTaskRequest.class);
                            validator.validate(request, "get_task_request");

                            final String currentUserId = ctx.get("aggregate_id");
                            return taskService.getTask(currentUserId, request);
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
            case "create_task" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), CreateTaskRequest.class);
                    validator.validate(request, "create_task_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return taskService.createTask(currentUserId, request);
                });
            }
            case "update_task_info" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), UpdateTaskInfoRequest.class);
                    validator.validate(request, "update_task_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return taskService.updateTask(currentUserId, request);
                });
            }
            case "update_task_status" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), UpdateTaskStatusRequest.class);
                    validator.validate(request, "update_task_status_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return taskService.updateTaskStatus(currentUserId, request);
                });
            }
            case "add_assignee" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), AddAssigneeRequest.class);
                    validator.validate(request, "add_assignee_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return taskService.addAssignee(currentUserId, request);
                });
            }
            case "delete_assignee" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), DeleteAssigneeRequest.class);
                    validator.validate(request, "delete_assignee_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return taskService.deleteAssignee(currentUserId, request);
                });
            }
            case "delete_task" -> {
                return Mono.deferContextual(ctx -> {
                    if (wsRequest.data() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data is required"));
                    }

                    final var request = SerializerUtils.deserializeFromJsonBytes(wsRequest.data(), DeleteTaskRequest.class);
                    validator.validate(request, "delete_task_request");

                    final String currentUserId = ctx.get("aggregate_id");
                    return taskService.deleteTask(currentUserId, request);
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