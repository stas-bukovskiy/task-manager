package org.tasker.task.output.persistance.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.tasker.task.model.domain.TaskDocument;
import org.tasker.task.output.persistance.CustomTaskRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
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

    @Override
    public Mono<TaskDocument> updateTaskInfo(TaskDocument taskDoc) {
        Update update = new Update();
        update.set("title", taskDoc.getTitle());
        update.set("description", taskDoc.getDescription());
        update.set("start_date", taskDoc.getStartDate());
        update.set("due_date", taskDoc.getDueDate());
        update.set("estimated_time", taskDoc.getEstimatedTime());
        update.set("priority", taskDoc.getPriority());
        return template.update(TaskDocument.class)
                .matching(Criteria.where("aggregate_id").is(taskDoc.getAggregateId()))
                .apply(update)
                .all()
                .doOnError(ex -> log.error("error occurred while adding invited user to board doc", ex))
                .doOnSuccess(v -> log.info("task info updated, task doc: {}}", taskDoc))
                .then(Mono.just(taskDoc));
    }

    @Override
    public Mono<TaskDocument> updateTaskStatus(TaskDocument taskDoc) {
        Update update = new Update();
        update.set("status", taskDoc.getStatus());
        return template.update(TaskDocument.class)
                .matching(Criteria.where("aggregate_id").is(taskDoc.getAggregateId()))
                .apply(update)
                .all()
                .doOnError(ex -> log.error("error occurred while adding invited user to board doc", ex))
                .doOnSuccess(v -> log.info("task status updated, task doc: {}, status: {}", taskDoc.getId(), taskDoc.getStatus()))
                .then(Mono.just(taskDoc));
    }

    @Override
    public Mono<TaskDocument> addAssigneeId(TaskDocument taskDoc, String assigneeId) {
        Update update = new Update().addToSet("assignee_ids", assigneeId);
        return template.update(TaskDocument.class)
                .matching(Criteria.where("aggregate_id").is(taskDoc.getAggregateId()))
                .apply(update)
                .all()
                .doOnError(ex -> log.error("error occurred while adding invited user to board doc", ex))
                .doOnSuccess(v -> log.info("assignee added to task doc, task doc: {}, assigneeId: {}", taskDoc.getId(), assigneeId))
                .then(Mono.just(taskDoc));
    }

    @Override
    public Mono<TaskDocument> deleteAssigneeId(TaskDocument taskDoc, String assigneeId) {
        Update update = new Update().pull("assignee_ids", assigneeId);
        return template.update(TaskDocument.class)
                .matching(Criteria.where("aggregate_id").is(taskDoc.getAggregateId()))
                .apply(update)
                .all()
                .doOnError(ex -> log.error("error occurred while adding invited user to board doc", ex))
                .doOnSuccess(v -> log.info("assignee deleted from task doc, task doc: {}, assigneeId: {}", taskDoc.getId(), assigneeId))
                .then(Mono.just(taskDoc));
    }
}
