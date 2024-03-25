package org.tasker.task.input.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.Event;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.es.Projection;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.domain.BoardAggregate;
import org.tasker.common.models.domain.BoardDocument;
import org.tasker.common.models.event.*;
import org.tasker.common.output.event.EventsMessagingSpecs;
import org.tasker.task.mapper.BoardMapper;
import org.tasker.task.output.persistance.BoardRepository;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class BoardProjection extends Projection {

    private final static String[] ROUTING_KEYS = new String[]{
            BoardAggregate.AGGREGATE_TYPE + ".*",
    };

    private final BoardRepository boardRepository;
    private final EventStoreDB eventStore;


    public BoardProjection(BoardRepository boardRepository, EventStoreDB eventStore, EventsMessagingSpecs messagingSpecs,
                           Receiver receiver) {
        super(receiver, messagingSpecs, Map.of(
                BoardCreatedEvent.BOARD_CREATED_V1, (event ->
                        boardRepository.findByAggregateId(event.getAggregateId())
                                .doOnNext(existed -> log.info("user doc <{}> already exist with such aggregateId: {}", existed.getId(), existed.getAggregateId()))
                                .switchIfEmpty(Mono.fromCallable(() -> {
                                            final var baseEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardCreatedEvent.class);
                                            return BoardDocument.builder()
                                                    .aggregateId(baseEvent.getAggregateId())
                                                    .title(baseEvent.getTitle())
                                                    .ownerId(baseEvent.getOwnerId())
                                                    .processedEvents(Set.of(event.getId().toString()))
                                                    .build();
                                        }).flatMap(boardRepository::insert)
                                        .doOnNext(inserted -> log.info("board doc <{}> created for aggregateId: {}", inserted.getId(), inserted.getAggregateId())))
                                .then()),
                UserInvitedEvent.USER_INVITED_V1, (event -> Mono.just(
                                SerializerUtils.deserializeFromJsonBytes(event.getData(), UserInvitedEvent.class)
                        )
                        .zipWhen(userInvitedEvent -> boardRepository.findByAggregateId(userInvitedEvent.getAggregateId())
                                .repeatWhenEmpty(3, (retrySpec) -> retrySpec.delayElements(Duration.of(1, ChronoUnit.SECONDS))))
                        .flatMap(tuple -> {
                            final var userInvitedEvent = tuple.getT1();
                            return boardRepository.addInvitedId(userInvitedEvent.getAggregateId(), userInvitedEvent.getToUserId(), event.getId().toString());
                        })
                        .doOnError(err -> log.error("error occurred while adding invited user to board doc", err))
                        .doOnSuccess(v -> log.info("invited user added to board doc"))
                        .then()
                ),
                InvitationReviewedEvent.INVITATION_REVIEWED_V1, (event -> Mono.just(
                                SerializerUtils.deserializeFromJsonBytes(event.getData(), InvitationReviewedEvent.class)
                        ).zipWhen(invitationReviewedEvent -> boardRepository.findByAggregateId(invitationReviewedEvent.getAggregateId()))
                        .flatMap(tuple -> {
                            final var invitationReviewedEvent = tuple.getT1();
                            final var boardDoc = tuple.getT2();

                            if (invitationReviewedEvent.isAccepted()) {
                                return boardRepository.addJoinedId(boardDoc.getAggregateId(), invitationReviewedEvent.getUserId(), event.getId().toString());
                            } else {
                                return boardRepository.removeInvitedId(boardDoc.getAggregateId(), invitationReviewedEvent.getUserId(), event.getId().toString());
                            }
                        })
                        .then()
                ),
                BoardDeletedEvent.BOARD_DELETED_V1, (event -> boardRepository.deleteByAggregateId(event.getAggregateId())
                        .doOnSuccess(v -> log.info("board doc deleted for aggregateId: {}", event.getAggregateId()))
                ),
                BoardMemberDeletedEvent.BOARD_MEMBER_DELETED_V1, (event -> Mono.just(
                                SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardMemberDeletedEvent.class)
                        )
                        .flatMap(boardMemberDeletedEvent -> boardRepository.removeInvitedId(boardMemberDeletedEvent.getAggregateId(), boardMemberDeletedEvent.getMemberId(), event.getId().toString())
                                .then(boardRepository.removeJoinedId(boardMemberDeletedEvent.getAggregateId(), boardMemberDeletedEvent.getMemberId(), event.getId().toString())))
                        .then()
                ),
                BoardUpdatedEvent.BOARD_UPDATED_V1, (event -> Mono.just(
                        SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardUpdatedEvent.class)
                ).flatMap(boardUpdatedEvent -> boardRepository.updateTitle(event.getAggregateId(), boardUpdatedEvent.getTitle(), event.getId().toString())
                        .doOnSuccess(v -> log.info("board doc title updated {}", boardUpdatedEvent)
                        ))
                ),
                InvitationDeletedEvent.INVITATION_DELETED_V1, (event -> Mono.just(
                        SerializerUtils.deserializeFromJsonBytes(event.getData(), InvitationDeletedEvent.class)
                ).flatMap(invitationDeletedEvent -> boardRepository.removeInvitedId(invitationDeletedEvent.getAggregateId(), invitationDeletedEvent.getUserId(), event.getId().toString())
                        .then(boardRepository.removeJoinedId(invitationDeletedEvent.getAggregateId(), invitationDeletedEvent.getUserId(), event.getId().toString())))
                )
        ), BoardAggregate.AGGREGATE_TYPE, ROUTING_KEYS);

        this.boardRepository = boardRepository;
        this.eventStore = eventStore;
    }

    @Override
    protected Mono<Void> handleError(Throwable err, Event event) {
        return boardRepository.deleteByAggregateId(event.getAggregateId())
                .then(eventStore.load(event.getAggregateId(), BoardAggregate.class))
                .map(BoardMapper::fromAggToDoc)
                .flatMap(boardRepository::insert)
                .doOnSuccess(boardDoc -> log.info("successfully restored board doc <{}> for aggregateId: {}", boardDoc.getId(), boardDoc.getAggregateId()))
                .then();
    }

}
