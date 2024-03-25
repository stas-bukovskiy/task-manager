package org.tasker.notification.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tasker.common.es.Event;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.exception.ItemNotFoundException;
import org.tasker.common.models.domain.NotificationAggregate;
import org.tasker.common.models.event.*;
import org.tasker.notification.models.domain.NotificationDocument;
import org.tasker.notification.output.persistance.NotificationRepository;
import org.tasker.notification.service.BoardService;
import org.tasker.notification.service.NotificationService;
import org.tasker.notification.service.TaskService;
import org.tasker.notification.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final TaskService taskService;
    private final BoardService boardService;
    private final UserService userService;
    private final EventStoreDB eventStore;
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(@Qualifier("notificationTaskService") TaskService taskService,
                                   @Qualifier("notificationBoardService") BoardService boardService,
                                   @Qualifier("notificationUserService") UserService userService,
                                   NotificationRepository notificationRepository, EventStoreDB eventStore) {
        this.taskService = taskService;
        this.boardService = boardService;
        this.userService = userService;
        this.eventStore = eventStore;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Mono<Void> processEvent(Event event) {
        switch (event.getEventType()) {
            case UserInvitedEvent.USER_INVITED_V1 -> {
                final var userInvitedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), UserInvitedEvent.class);
                return process(userInvitedEvent);
            }
            case InvitationReviewedEvent.INVITATION_REVIEWED_V1 -> {
                final var invitationDeletedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), InvitationReviewedEvent.class);
                return process(invitationDeletedEvent);
            }
            case InvitationDeletedEvent.INVITATION_DELETED_V1 -> {
                final var invitationDeletedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), InvitationDeletedEvent.class);
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
            case TaskInfoUpdatedEvent.TASK_INFO_UPDATED_V1 -> {
                final var taskInfoUpdatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskInfoUpdatedEvent.class);
                return process(taskInfoUpdatedEvent);
            }
            case TaskStatusUpdatedEvent.TASK_STATUS_UPDATED_V1 -> {
                final var taskStatusUpdatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), TaskStatusUpdatedEvent.class);
                return process(taskStatusUpdatedEvent);
            }
            default -> {
                log.info("Unknown event type for notification: {}", event);
                return Mono.empty();
            }
        }
    }

    @Override
    public Mono<NotificationDocument> getNotification(String userId, String fotAggId, String type) {
        return notificationRepository.findByUserIdAndForAggregateIdAndForAggregateTypeAndValid(userId, fotAggId, type, true)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Notification for user %s, for %s, type %s not found", userId, fotAggId, type)));
    }

    @Override
    public Mono<NotificationDocument> getNotification(String aggId) {
        return notificationRepository.findByAggregateId(aggId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Notification %s not found", aggId)));
    }

    private Mono<Void> process(UserInvitedEvent event) {
        return boardService.getBoard(event.getAggregateId())
                .zipWith(userService.getUser(event.getFromUserId()))
                .flatMap(tuple -> {
                    final var board = tuple.getT1();
                    final var owner = tuple.getT2();

                    final var notification = new NotificationAggregate(UUID.randomUUID().toString());
                    notification.createNotification(event.getToUserId(), board.getAggregateId(),
                            String.format("You have invited to join '%s' board by @%s", board.getTitle(), owner.getUsername()),
                            UserInvitedEvent.USER_INVITED_V1);
                    return eventStore.save(notification);
                }).then();
    }

    private Mono<Void> process(InvitationReviewedEvent event) {
        return boardService.getBoard(event.getAggregateId())
                .zipWhen(board -> userService.getUser(board.getOwnerId()))
                .zipWith(userService.getUser(event.getUserId()), (boardOwnerTuple, invitedUser) ->
                        Tuples.of(boardOwnerTuple.getT1(), boardOwnerTuple.getT2(), invitedUser))
                .flatMap(tuple -> {
                    var board = tuple.getT1();
                    var owner = tuple.getT2();
                    var user = tuple.getT3();


                    final var notificationForOwner = new NotificationAggregate(UUID.randomUUID().toString());
                    notificationForOwner.createNotification(owner.getAggregateId(), board.getAggregateId(),
                            String.format("@%s has %s your invitation to join '%s' board", user.getUsername(), event.isAccepted() ? "accepted" : "declined", board.getTitle()),
                            InvitationReviewedEvent.INVITATION_REVIEWED_V1);
                    return eventStore.save(notificationForOwner)
                            .then(Mono.just(board));
                })
                .flatMap(board -> invalidateNotification(event.getUserId(), board.getAggregateId(), UserInvitedEvent.USER_INVITED_V1))
                .onErrorComplete();
    }

    @Override
    public Mono<Void> invalidateNotification(String userId, String fotAggId, String type) {
        return getNotification(userId, fotAggId, type)
                .onErrorResume(e -> {
                    log.error("Error while invalidating notification", e);
                    return Mono.empty();
                })
                .flatMap(notification -> eventStore.load(notification.getAggregateId(), NotificationAggregate.class))
                .flatMap(notificationAgg -> {
                    notificationAgg.invalidate();
                    return eventStore.save(notificationAgg);
                });
    }

    private Mono<Void> process(InvitationDeletedEvent event) {
        return boardService.getBoard(event.getAggregateId())
                .flatMap(board -> invalidateNotification(event.getUserId(), board.getAggregateId(), UserInvitedEvent.USER_INVITED_V1))
                .onErrorComplete();
    }

    private Mono<Void> process(TaskStatusUpdatedEvent taskStatusUpdatedEvent) {
        return taskService.getTask(taskStatusUpdatedEvent.getAggregateId())
                .flatMapMany(task -> {
                    String ownerId = task.getBoard().getOwnerId();
                    Set<String> toUserIds = new HashSet<>(task.getAssigneeIds());
                    toUserIds.add(ownerId);

                    var message = String.format("Task '%s' status has been updated to %s", task.getTitle(), taskStatusUpdatedEvent.getStatus());

                    return Flux.fromIterable(toUserIds).flatMap(userId -> {
                        final var notification = new NotificationAggregate(UUID.randomUUID().toString());
                        notification.createNotification(userId, task.getAggregateId(), message, TaskStatusUpdatedEvent.TASK_STATUS_UPDATED_V1);
                        return eventStore.save(notification);
                    });
                }).then();
    }

    private Mono<Void> process(TaskInfoUpdatedEvent taskInfoUpdatedEvent) {
        return taskService.getTask(taskInfoUpdatedEvent.getAggregateId())
                .flatMapMany(task -> {
                    String ownerId = task.getBoard().getOwnerId();
                    Set<String> toUserIds = new HashSet<>(task.getAssigneeIds());
                    toUserIds.add(ownerId);

                    var message = String.format("Task '%s' has been updated", task.getTitle());

                    return Flux.fromIterable(toUserIds).flatMap(userId -> {
                        final var notification = new NotificationAggregate(UUID.randomUUID().toString());
                        notification.createNotification(userId, task.getAggregateId(), message, TaskInfoUpdatedEvent.TASK_INFO_UPDATED_V1);
                        return eventStore.save(notification);
                    });
                }).then();
    }

    private Mono<Void> process(TaskAssigneeAdded taskAssigneeAdded) {
        return taskService.getTask(taskAssigneeAdded.getAggregateId())
                .publishOn(Schedulers.boundedElastic())
                .flatMapMany(task -> {
                    String ownerId = task.getBoard().getOwnerId();
                    Set<String> toUserIds = new HashSet<>(task.getAssigneeIds());
                    toUserIds.add(ownerId);
                    toUserIds.add(taskAssigneeAdded.getAssigneeId());

                    final var assignee = userService.getUser(taskAssigneeAdded.getAssigneeId()).block();
                    return Flux.fromIterable(toUserIds).flatMap(userId -> {
                        final var notification = new NotificationAggregate(UUID.randomUUID().toString());

                        String message;
                        if (userId.equals(taskAssigneeAdded.getAssigneeId())) {
                            message = String.format("You have been assigned to task '%s'", task.getTitle());
                        } else {
                            message = String.format("@%s has been assigned to task '%s'", Objects.requireNonNull(assignee).getUsername(), task.getTitle());
                        }

                        notification.createNotification(userId, task.getAggregateId(), message, TaskAssigneeAdded.TASK_ASSIGNEE_ADDED_V1);
                        return eventStore.save(notification);
                    });
                }).then();
    }


    private Mono<Void> process(TaskAssigneeDeleted taskAssigneeDeleted) {
        return taskService.getTask(taskAssigneeDeleted.getAggregateId())
                .flatMapMany(task -> {
                    String ownerId = task.getBoard().getOwnerId();
                    Set<String> toUserIds = new HashSet<>(task.getAssigneeIds());
                    toUserIds.add(ownerId);

                    return Flux.fromIterable(toUserIds).flatMap(userId -> {
                        final var notification = new NotificationAggregate(UUID.randomUUID().toString());

                        String message;
                        if (userId.equals(taskAssigneeDeleted.getAssigneeId())) {
                            message = String.format("You have been unassigned from task '%s'", task.getTitle());
                        } else {
                            message = String.format("@%s has been unassigned from task '%s'", taskAssigneeDeleted.getAssigneeId(), task.getTitle());
                        }

                        notification.createNotification(userId, task.getAggregateId(), message, TaskAssigneeDeleted.TASK_ASSIGNEE_DELETED_V1);
                        return eventStore.save(notification);
                    });
                }).then();
    }
}
