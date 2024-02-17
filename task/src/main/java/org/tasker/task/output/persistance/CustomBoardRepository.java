package org.tasker.task.output.persistance;

import reactor.core.publisher.Mono;

public interface CustomBoardRepository {

    Mono<Void> addInvitedId(String aggregateId, String invitedId, String processedEventId);

}
