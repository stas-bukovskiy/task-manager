package org.tasker.task.mapper;

import org.tasker.common.es.Event;
import org.tasker.common.models.domain.BoardAggregate;
import org.tasker.common.models.domain.BoardDocument;
import org.tasker.common.models.dto.BoardDto;

import java.util.ArrayList;
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

    public static BoardDto fromDocToDto(BoardDocument doc) {
        return BoardDto.builder()
                .id(doc.getAggregateId())
                .title(doc.getTitle())
                .ownerId(doc.getOwnerId())
                .invitedIds(doc.getInvitedIds() == null ? new ArrayList<>() : new ArrayList<>(doc.getInvitedIds()))
                .joinedIds(doc.getJoinedIds() == null ? new ArrayList<>() : new ArrayList<>(doc.getJoinedIds()))
                .build();
    }
}
