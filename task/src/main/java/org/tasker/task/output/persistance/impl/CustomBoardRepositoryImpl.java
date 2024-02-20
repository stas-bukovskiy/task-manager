package org.tasker.task.output.persistance.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.tasker.task.model.domain.BoardDocument;
import org.tasker.task.output.persistance.CustomBoardRepository;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class CustomBoardRepositoryImpl implements CustomBoardRepository {

    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Void> addInvitedId(String aggregateId, String invitedId, String processedEventId) {
        Update update = new Update().push("invited_ids", invitedId);
        update.push("processed_events", processedEventId);
        return template.update(BoardDocument.class)
                .matching(Criteria.where("aggregate_id").is(aggregateId))
                .apply(update)
                .all()
                .doOnError(ex -> log.error(" error occurred while adding invited user to board doc", ex))
                .doOnSuccess(v -> log.info("invited user added to board doc"))
                .then();
    }

    @Override
    public Mono<Void> addJoinedId(String aggregateId, String joinedUserId, String processedEventId) {
        Update update = new Update().push("joined_ids", joinedUserId);
        update.push("processed_events", processedEventId);
        update.pull("invited_ids", joinedUserId);
        return template.update(BoardDocument.class)
                .matching(Criteria.where("aggregate_id").is(aggregateId))
                .apply(update)
                .all()
                .doOnError(ex -> log.error(" error occurred while adding joined user to board doc", ex))
                .doOnSuccess(v -> log.info("joined user added to board doc"))
                .then();
    }

    @Override
    public Mono<Void> removeInvitedId(String aggregateId, String investedUserId, String processedEventId) {
        Update update = new Update().pull("invited_ids", investedUserId);
        update.push("processed_events", processedEventId);
        return template.update(BoardDocument.class)
                .matching(Criteria.where("aggregate_id").is(aggregateId))
                .apply(update)
                .all()
                .doOnError(ex -> log.error(" error occurred while removing invited user from board doc", ex))
                .doOnSuccess(v -> log.info("invited user removed from board doc"))
                .then();
    }

}
