package org.tasker.common.models.domain;

import lombok.*;
import org.tasker.common.es.AggregateRoot;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.event.BoardCreatedEvent;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class BoardAggregate extends AggregateRoot {

    public static final String AGGREGATE_TYPE = "board_aggregate";

    private String title;
    private String ownerId;
    private List<String> invitedIds;
    private List<String> joinedIds;

    public BoardAggregate(String id) {
        super(id, AGGREGATE_TYPE);
    }

    @Override
    public void when(Event event) {
        switch (event.getEventType()) {
            case BoardCreatedEvent.BOARD_CREATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardCreatedEvent.class));
        }
    }

    private void handle(BoardCreatedEvent event) {
        this.title = event.getTitle();
        this.ownerId = event.getOwnerId();
        this.invitedIds = new LinkedList<>();
        this.joinedIds = new LinkedList<>();
    }

    public void createBoard(String title, String ownerId) {
        final var data = BoardCreatedEvent.builder()
                .aggregateId(id)
                .title(title)
                .ownerId(ownerId)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(BoardCreatedEvent.BOARD_CREATED_V1, dataBytes);
        this.apply(event);
    }

}
