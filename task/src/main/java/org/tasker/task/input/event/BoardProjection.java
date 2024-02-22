package org.tasker.task.input.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.Event;
import org.tasker.common.es.EventStoreDB;
import org.tasker.common.es.Projection;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.domain.BoardAggregate;
import org.tasker.common.models.event.BoardCreatedEvent;
import org.tasker.common.models.event.InvitationReviewedEvent;
import org.tasker.common.models.event.UserInvitedEvent;
import org.tasker.common.output.event.EventsMessagingSpecs;
import org.tasker.task.mapper.BoardMapper;
import org.tasker.task.model.domain.BoardDocument;
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
            UserInvitedEvent.AGGREGATE_TYPE + "." + UserInvitedEvent.USER_INVITED_V1,
            InvitationReviewedEvent.AGGREGATE_TYPE + "." + InvitationReviewedEvent.INVITATION_REVIEWED_V1
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
                        .zipWhen(userInvitedEvent -> boardRepository.findByAggregateId(userInvitedEvent.getBoardId())
                                .repeatWhenEmpty(3, (retrySpec) -> retrySpec.delayElements(Duration.of(1, ChronoUnit.SECONDS))))
                        .flatMap(tuple -> {
                            final var userInvitedEvent = tuple.getT1();
                            return boardRepository.addInvitedId(userInvitedEvent.getBoardId(), userInvitedEvent.getToUserId(), event.getId().toString());
                        })
                        .doOnError(err -> log.error("error occurred while adding invited user to board doc", err))
                        .doOnSuccess(v -> log.info("invited user added to board doc"))
                        .then()
                ),
                InvitationReviewedEvent.INVITATION_REVIEWED_V1, (event -> Mono.just(
                                SerializerUtils.deserializeFromJsonBytes(event.getData(), InvitationReviewedEvent.class)
                        ).zipWhen(invitationReviewedEvent -> boardRepository.findByAggregateId(invitationReviewedEvent.getBoardId()))
                        .flatMap(tuple -> {
                            final var invitationReviewedEvent = tuple.getT1();
                            final var boardDoc = tuple.getT2();

                            if (invitationReviewedEvent.isAccepted()) {
                                return boardRepository.addJoinedId(boardDoc.getAggregateId(), invitationReviewedEvent.getToUserId(), event.getId().toString());
                            } else {
                                return boardRepository.removeInvitedId(boardDoc.getAggregateId(), invitationReviewedEvent.getToUserId(), event.getId().toString());
                            }
                        })
                        .then()
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
