package org.tasker.notification.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.dto.UpdateDto;
import org.tasker.common.models.event.*;
import org.tasker.notification.service.BoardService;
import org.tasker.notification.service.NotificationService;
import org.tasker.notification.service.TaskService;
import org.tasker.notification.service.UpdateService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("notificationUpdateService")
public class UpdateServiceImpl implements UpdateService {

    private final BoardService boardService;
    private final TaskService taskService;
    private final NotificationService notificationService;

    public UpdateServiceImpl(@Qualifier("notificationBoardService") BoardService boardService,
                             @Qualifier("notificationTaskService") TaskService taskService,
                             NotificationService notificationService) {
        this.boardService = boardService;
        this.taskService = taskService;
        this.notificationService = notificationService;
    }


    @Override
    public Flux<UpdateDto<?>> processEvent(Event event) {
        switch (event.getEventType()) {
            case BoardCreatedEvent.BOARD_CREATED_V1 -> {
                final var boardCreatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardCreatedEvent.class);
                return process(boardCreatedEvent);
            }
            case BoardDeletedEvent.BOARD_DELETED_V1 -> {
                final var boardDeletedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardDeletedEvent.class);
                return process(boardDeletedEvent);
            }
            case BoardMemberDeletedEvent.BOARD_MEMBER_DELETED_V1 -> {
                final var boardMemberDeletedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardMemberDeletedEvent.class);
                return process(boardMemberDeletedEvent);
            }
            case BoardUpdatedEvent.BOARD_UPDATED_V1 -> {
                final var boardUpdatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardUpdatedEvent.class);
                return process(boardUpdatedEvent);
            }
            case UserInvitedEvent.USER_INVITED_V1 -> {
                final var userInvitedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), UserInvitedEvent.class);
                return process(userInvitedEvent);
            }
            case InvitationDeletedEvent.INVITATION_DELETED_V1 -> {
                final var invitationDeletedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), InvitationDeletedEvent.class);
                return process(invitationDeletedEvent);
            }
            case InvitationReviewedEvent.INVITATION_REVIEWED_V1 -> {
                final var invitationDeletedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), InvitationReviewedEvent.class);
                return process(invitationDeletedEvent);
            }
            case TaskAssigneeAdded.TASK_ASSIGNEE_ADDED_V1 -> {
                final var taskAssigneeAdded = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskAssigneeAdded.class);
                return process(taskAssigneeAdded);
            }
            case TaskAssigneeDeleted.TASK_ASSIGNEE_DELETED_V1 -> {
                final var taskAssigneeDeleted = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskAssigneeDeleted.class);
                return process(taskAssigneeDeleted);
            }
            case TaskCreatedEvent.TASK_CREATED_V1 -> {
                final var taskCreatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskCreatedEvent.class);
                return process(taskCreatedEvent);
            }
            case TaskDeletedEvent.TASK_DELETED_V1 -> {
                final var taskDeletedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskDeletedEvent.class);
                return process(taskDeletedEvent);
            }
            case TaskInfoUpdatedEvent.TASK_INFO_UPDATED_V1 -> {
                final var taskInfoUpdatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskInfoUpdatedEvent.class);
                return process(taskInfoUpdatedEvent);
            }
            case TaskStatusUpdatedEvent.TASK_STATUS_UPDATED_V1 -> {
                final var taskStatusUpdatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskStatusUpdatedEvent.class);
                return process(taskStatusUpdatedEvent);
            }
            case NotificationCreatedEvent.NOTIFICATION_CREATED_V1 -> {
                final var notificationCreatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), NotificationCreatedEvent.class);
                return process(notificationCreatedEvent);
            }
            case NotificationInvalidatedEvent.NOTIFICATION_INVALIDATED_V1 -> {
                final var notificationInvalidatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), NotificationInvalidatedEvent.class);
                return process(notificationInvalidatedEvent);
            }
            default -> {
                log.info("Unknown event type for update: {}", event);
                return Flux.empty();
            }
        }
    }


    private Flux<UpdateDto<?>> process(BoardMemberDeletedEvent boardMemberDeletedEvent) {
        return boardService.getBoardOnlineUserIds(boardMemberDeletedEvent.getAggregateId())
                .collectList()
                .flatMapMany(toUserIds -> Flux.just(UpdateDto.builder()
                        .toUserIds(toUserIds)
                        .type(BoardMemberDeletedEvent.BOARD_MEMBER_DELETED_V1)
                        .data(boardMemberDeletedEvent)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(UserInvitedEvent userInvitedEvent) {
        return boardService.getBoard(userInvitedEvent.getAggregateId())
                .flatMapMany(board -> Flux.just(UpdateDto.builder()
                        .toUserIds(List.of(board.getOwnerId()))
                        .type(UserInvitedEvent.USER_INVITED_V1)
                        .data(userInvitedEvent)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(NotificationInvalidatedEvent event) {
        return notificationService.getNotification(event.getAggregateId())
                .flatMapMany(notification -> Flux.just(UpdateDto.builder()
                        .toUserIds(List.of(notification.getUserId()))
                        .type(NotificationInvalidatedEvent.NOTIFICATION_INVALIDATED_V1)
                        .data(event)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(NotificationCreatedEvent notificationCreatedEvent) {
        return Flux.just(UpdateDto.builder()
                .toUserIds(List.of(notificationCreatedEvent.getUserId()))
                .type(NotificationCreatedEvent.NOTIFICATION_CREATED_V1)
                .data(notificationCreatedEvent)
                .build());
    }

    private Flux<UpdateDto<?>> process(TaskStatusUpdatedEvent taskStatusUpdatedEvent) {
        return taskService.getTask(taskStatusUpdatedEvent.getAggregateId())
                .flatMapMany(task -> boardService.getBoardOnlineUserIds(task.getBoardId()))
                .collectList()
                .flatMapMany(userIds -> Flux.just(UpdateDto.builder()
                        .toUserIds(userIds)
                        .type(TaskStatusUpdatedEvent.TASK_STATUS_UPDATED_V1)
                        .data(taskStatusUpdatedEvent)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(TaskInfoUpdatedEvent taskInfoUpdatedEvent) {
        return taskService.getTask(taskInfoUpdatedEvent.getAggregateId())
                .flatMapMany(task -> boardService.getBoardOnlineUserIds(task.getBoardId()))
                .collectList()
                .flatMapMany(userIds -> Flux.just(UpdateDto.builder()
                        .toUserIds(userIds)
                        .type(TaskInfoUpdatedEvent.TASK_INFO_UPDATED_V1)
                        .data(taskInfoUpdatedEvent)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(TaskDeletedEvent taskDeletedEvent) {
        return boardService.getBoardOnlineUserIds(taskDeletedEvent.getBoardId())
                .collectList()
                .flatMapMany(userIds -> Flux.just(UpdateDto.builder()
                        .toUserIds(userIds)
                        .type(TaskDeletedEvent.TASK_DELETED_V1)
                        .data(taskDeletedEvent)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(TaskCreatedEvent taskCreatedEvent) {
        return boardService.getBoardOnlineUserIds(taskCreatedEvent.getBoardId())
                .collectList()
                .flatMapMany(userIds -> Flux.just(UpdateDto.builder()
                        .toUserIds(userIds)
                        .type(TaskCreatedEvent.TASK_CREATED_V1)
                        .data(taskCreatedEvent)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(TaskAssigneeDeleted taskAssigneeDeleted) {
        return taskService.getTask(taskAssigneeDeleted.getAggregateId())
                .flatMapMany(task -> boardService.getBoardOnlineUserIds(task.getBoardId()))
                .collectList()
                .flatMapMany(userIds -> Flux.just(UpdateDto.builder()
                        .toUserIds(userIds)
                        .type(TaskAssigneeDeleted.TASK_ASSIGNEE_DELETED_V1)
                        .data(taskAssigneeDeleted)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(TaskAssigneeAdded event) {
        return taskService.getTask(event.getAggregateId())
                .flatMapMany(task -> boardService.getBoardOnlineUserIds(task.getBoardId()))
                .collectList()
                .flatMapMany(userIds -> Flux.just(UpdateDto.builder()
                        .toUserIds(userIds)
                        .type(TaskAssigneeAdded.TASK_ASSIGNEE_ADDED_V1)
                        .data(event)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(InvitationReviewedEvent event) {
        return Mono.just(UpdateDto.builder()
                        .type(InvitationReviewedEvent.INVITATION_REVIEWED_V1)
                        .data(event))
                .flatMapMany(updateDto -> {
                    if (event.isAccepted()) {
                        return boardService.getBoardOnlineUserIds(event.getAggregateId())
                                .collectList()
                                .flatMapMany(userIds -> {
                                    final var toUserIds = new ArrayList<>(userIds);
                                    toUserIds.add(event.getUserId());
                                    return Flux.just(updateDto
                                            .toUserIds(toUserIds)
                                            .build());
                                });
                    } else {
                        return boardService.getBoard(event.getAggregateId())
                                .flatMapMany(board -> Flux.just(updateDto
                                        .toUserIds(List.of(board.getOwnerId()))
                                        .build()));
                    }
                });
    }

    private Flux<UpdateDto<?>> process(InvitationDeletedEvent event) {
        return boardService.getBoard(event.getAggregateId())
                .flatMapMany(board -> Flux.just(UpdateDto.builder()
                        .toUserIds(List.of(board.getOwnerId()))
                        .type(InvitationDeletedEvent.INVITATION_DELETED_V1)
                        .data(event)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(BoardUpdatedEvent event) {
        return boardService.getBoardOnlineUserIds(event.getAggregateId())
                .collectList()
                .flatMapMany(toUserIds -> Flux.just(UpdateDto.builder()
                        .toUserIds(toUserIds)
                        .type(BoardUpdatedEvent.BOARD_UPDATED_V1)
                        .data(event)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(BoardDeletedEvent event) {
        return Flux.fromIterable(event.getToUserIds())
                .flatMap(toUserId -> notificationService.invalidateNotification(toUserId, event.getAggregateId(), UserInvitedEvent.USER_INVITED_V1)
                        .onErrorContinue((e, ignored) -> log.info("Failed to invalidate notifications for board deletion", e)))
                .then(Mono.just(event.getToUserIds()))
                .flatMapMany(toUserIds -> Flux.just(UpdateDto.builder()
                        .toUserIds(toUserIds.stream().toList())
                        .type(BoardDeletedEvent.BOARD_DELETED_V1)
                        .data(event)
                        .build()));
    }

    private Flux<UpdateDto<?>> process(BoardCreatedEvent boardCreatedEvent) {
        return Flux.just(UpdateDto.builder()
                .toUserIds(List.of(boardCreatedEvent.getOwnerId()))
                .type(BoardCreatedEvent.BOARD_CREATED_V1)
                .data(boardCreatedEvent)
                .build());
    }

}
