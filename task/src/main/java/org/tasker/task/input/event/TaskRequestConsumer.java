package org.tasker.task.input.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.exceptions.NotPermittedException;
import org.tasker.common.models.commands.*;
import org.tasker.common.models.dto.Statistic;
import org.tasker.common.models.queries.*;
import org.tasker.common.models.response.GetBoardsResponse;
import org.tasker.common.models.response.GetStatisticResponse;
import org.tasker.common.models.response.GetTaskResponse;
import org.tasker.common.models.response.GetTasksResponse;
import org.tasker.task.exception.ExpectedException;
import org.tasker.task.exception.ItemNotFoundException;
import org.tasker.task.service.BoardService;
import org.tasker.task.service.InvitationService;
import org.tasker.task.service.TaskService;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.util.ArrayList;
import java.util.Arrays;

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
                        case DeleteBoardCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), DeleteBoardCommand.class));
                        case DeleteInvitationCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), DeleteInvitationCommand.class));
                        case DeleteMemberCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), DeleteMemberCommand.class));
                        case UpdateBoardCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), UpdateBoardCommand.class));
                        case InviteUsersCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), InviteUsersCommand.class));
                        case GetTasksQuery.QUERY_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), GetTasksQuery.class), delivery.getProperties().getReplyTo());
                        case GetTaskQuery.QUERY_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), GetTaskQuery.class), delivery.getProperties().getReplyTo());
                        case DeleteAssigneeCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), DeleteAssigneeCommand.class));
                        case AddAssigneeCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), AddAssigneeCommand.class));
                        case DeleteTaskCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), DeleteTaskCommand.class));
                        case CreateTaskCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), CreateTaskCommand.class));
                        case UpdateTaskInfoCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), UpdateTaskInfoCommand.class));
                        case UpdateTaskStatusCommand.COMMAND_NAME ->
                                handle(SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), UpdateTaskStatusCommand.class));
                        default -> log.error("Unknown command: {}", commandName);
                    }
                });
    }

    private void handle(InviteUsersCommand command) {
        invitationService.inviteUsersToBoard(command)
                .doOnError(ex -> onError("Failed to invite users: {}", ex, command))
                .doOnSuccess(v -> log.info("Invited users <{}> to board <{}> for user <{}>", command.toUserIds(), command.boardId(), command.fromUserId()))
                .subscribe();
    }

    private void handle(UpdateBoardCommand command) {
        boardService.updateBoard(command.boardId(), command.userId(), command.title())
                .doOnError(ex -> onError("Failed to update board: {}", ex, command))
                .doOnSuccess(v -> log.info("Updated board <{}> for user <{}>", command.boardId(), command.userId()))
                .subscribe();
    }


    private void handle(DeleteMemberCommand command) {
        boardService.deleteMember(command.boardId(), command.userId(), command.memberId())
                .doOnError(ex -> onError("Failed to delete member: {}", ex, command))
                .doOnSuccess(v -> log.info("Deleted member <{}> from board <{}> for user <{}>", command.memberId(), command.boardId(), command.userId()))
                .subscribe();
    }

    private void handle(DeleteInvitationCommand command) {
        invitationService.deleteInvitation(command)
                .doOnError(ex -> onError("Failed to delete invitation: {}", ex, command))
                .doOnSuccess(v -> log.info("Deleted invitation for board <{}> and user <{}>", command.boardId(), command.userId()))
                .subscribe();
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
                .doOnError(ex -> onError("Failed to create board: {}", ex, command))
                .doOnSuccess(v -> log.info("Created new board '{}' for user <{}>", command.title(), command.ownerId()))
                .subscribe();
    }

    private void handle(ReviewInvitationCommand command) {
        invitationService.reviewInvitation(command)
                .doOnError(ex -> onError("Failed to review invitation: {}", ex, command))
                .doOnSuccess(v -> log.info("Reviewed invitation <{}> for user <{}>", command.boardId(), command.userId()))
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
                    if (ex instanceof ItemNotFoundException) {
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

    private void handle(DeleteBoardCommand command) {
        boardService.deleteBoard(command.boardId(), command.userId())
                .doOnError(ex -> onError("Failed to delete board: {}", ex, command))
                .doOnSuccess(v -> log.info("Deleted board <{}> for user <{}>", command.boardId(), command.userId()))
                .subscribe();
    }

    private void handle(GetTasksQuery query, String routingKey) {
        taskService.getTasks(query.userId(), query.boardId())
                .map(tasks -> GetTasksResponse.builder()
                        .httpCode(HttpStatus.OK.value())
                        .data(tasks)
                        .build())
                .onErrorResume(ex -> {
                            if (ex instanceof ItemNotFoundException) {
                                return Mono.just(GetTasksResponse.builder()
                                        .httpCode(HttpStatus.NOT_FOUND.value())
                                        .message(ex.getMessage())
                                        .build());
                            } else if (ex instanceof NotPermittedException) {
                                return Mono.just(GetTasksResponse.builder()
                                        .httpCode(HttpStatus.UNAUTHORIZED.value())
                                        .message(ex.getMessage())
                                        .build());
                            }

                            log.error("Error while handling query", ex);
                            return Mono.just(GetTasksResponse.builder()
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

    private void handle(GetTaskQuery query, String routingKey) {
        taskService.getTask(query.userId(), query.taskId())
                .map(tasks -> GetTaskResponse.builder()
                        .httpCode(HttpStatus.OK.value())
                        .data(tasks)
                        .build())
                .onErrorResume(ex -> {
                            if (ex instanceof ItemNotFoundException) {
                                return Mono.just(GetTaskResponse.builder()
                                        .httpCode(HttpStatus.NOT_FOUND.value())
                                        .message(ex.getMessage())
                                        .build());
                            } else if (ex instanceof NotPermittedException) {
                                return Mono.just(GetTaskResponse.builder()
                                        .httpCode(HttpStatus.UNAUTHORIZED.value())
                                        .message(ex.getMessage())
                                        .build());
                            }
                            log.error("Error while handling query", ex);
                            return Mono.just(GetTaskResponse.builder()
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

    private void handle(DeleteAssigneeCommand command) {
        taskService.deleteAssignee(command)
                .doOnError(ex -> onError("Failed to delete assignee: {}", ex, command))
                .doOnSuccess(v -> log.info("Deleted assignee <{}> from task <{}> for user <{}>", command.assigneeId(), command.taskId(), command.userId()))
                .subscribe();
    }

    private void handle(AddAssigneeCommand command) {
        taskService.addAssignee(command)
                .doOnError(ex -> onError("Failed to add assignee: {}", ex, command))
                .doOnSuccess(v -> log.info("Added assignee <{}> to task <{}> for user <{}>", command.assigneeId(), command.taskId(), command.userId()))
                .subscribe();
    }

    private void handle(DeleteTaskCommand command) {
        taskService.deleteTask(command)
                .doOnError(ex -> onError("Failed to delete task: {}", ex, command))
                .doOnSuccess(v -> log.info("Deleted task <{}> for user <{}>", command.taskId(), command.userId()))
                .subscribe();
    }

    private void handle(CreateTaskCommand command) {
        taskService.createTask(command)
                .doOnError(ex -> onError("Failed to create task: {}", ex, command))
                .doOnSuccess(v -> log.info("Created new task '{}' for user <{}>", command.title(), command.userId()))
                .subscribe();
    }

    private void handle(UpdateTaskInfoCommand command) {
        taskService.updateTaskInfo(command)
                .doOnError(ex -> onError("Failed to update task info: {}", ex, command))
                .doOnSuccess(v -> log.info("Updated task <{}> for user <{}>", command.taskId(), command.userId()))
                .subscribe();
    }

    private void handle(UpdateTaskStatusCommand command) {
        taskService.updateTaskStatus(command)
                .doOnError(ex -> onError("Failed to update task status: {}", ex, command))
                .doOnSuccess(v -> log.info("Updated task <{}> status for user <{}>", command.taskId(), command.userId()))
                .subscribe();
    }

    private void onError(String message, Throwable ex, Object... args) {
        var argsList = new ArrayList<>(Arrays.asList(args));
        argsList.add(ex);
        if (ex instanceof ExpectedException) {
            log.warn(message, argsList.toArray());
        } else {
            log.error(message, argsList.toArray());
        }
    }
}