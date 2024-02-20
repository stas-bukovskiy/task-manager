package org.tasker.task.output.persistance;

import reactor.core.publisher.Mono;

public interface CustomBoardRepository {

    Mono<Void> addInvitedId(String aggregateId, String invitedId, String processedEventId);

    Mono<Void> addJoinedId(String aggregateId, String joinedU, String processedEventId);

    Mono<Void> removeInvitedId(String aggregateId, String invitedUserId, String processedEventId);
}
