package org.tasker.task.mapper;

import org.tasker.common.es.Event;
import org.tasker.common.models.domain.BoardAggregate;
import org.tasker.task.model.domain.BoardDocument;

import java.util.HashSet;
import java.util.stream.Collectors;

public final class BoardMapper {

    private BoardMapper() {
    }

    public static BoardDocument fromAggToDoc(BoardAggregate agg) {
        return BoardDocument.builder()
                .aggregateId(agg.getId())
                .title(agg.getTitle())
                .ownerId(agg.getOwnerId())
                .invitedIds(new HashSet<>(agg.getInvitedIds()))
                .joinedIds(new HashSet<>(agg.getJoinedIds()))
                .processedEvents(agg.getChanges().stream()
                        .map(Event::getAggregateId)
                        .collect(Collectors.toSet()))
                .build();
    }
}
