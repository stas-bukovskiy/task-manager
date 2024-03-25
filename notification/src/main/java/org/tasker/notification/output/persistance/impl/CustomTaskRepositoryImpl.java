package org.tasker.notification.output.persistance.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.tasker.common.models.domain.TaskDocument;
import org.tasker.notification.output.persistance.CustomTaskRepository;
import reactor.core.publisher.Mono;

@Repository("notificationCustomTaskRepository")
@RequiredArgsConstructor
public class CustomTaskRepositoryImpl implements CustomTaskRepository {

    private final ReactiveMongoTemplate template;

    @Override
    public Mono<TaskDocument> findByAggregateId(String aggregateId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("aggregate_id").is(aggregateId)),
                Aggregation.lookup("boards", "board_id", "aggregate_id", "board"),
                Aggregation.unwind("board", true)
        );

        return template.aggregate(aggregation, "tasks", TaskDocument.class)
                .next();
    }
}
