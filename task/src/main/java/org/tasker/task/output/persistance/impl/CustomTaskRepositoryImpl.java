package org.tasker.task.output.persistance.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.tasker.common.models.domain.BoardDocument;
import org.tasker.common.models.domain.TaskDocument;
import org.tasker.common.models.enums.TaskStatus;
import org.tasker.task.model.domain.TaskStatisticProjection;
import org.tasker.task.output.persistance.CustomTaskRepository;
import reactor.core.publisher.Flux;
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

    @Override
    public Mono<TaskStatisticProjection> countAllTasksByStatusForUser(String userId) {
        return getUserBoardIds(userId).collectList().flatMap(boardIds -> {
            MatchOperation matchOperation = Aggregation.match(Criteria.where("board_id").in(boardIds));
            GroupOperation groupOperation = constructTaskStatusGroupOperation();

            Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);
            return template.aggregate(aggregation, "tasks", TaskStatisticProjection.class)
                    .next()
                    .switchIfEmpty(Mono.just(new TaskStatisticProjection()));
        });
    }

    @Override
    public Mono<TaskStatisticProjection> countAssignedTasksByStatusForUser(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("assignee_ids").all(userId));
        GroupOperation groupOperation = constructTaskStatusGroupOperation();

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);
        return template.aggregate(aggregation, "tasks", TaskStatisticProjection.class)
                .next()
                .switchIfEmpty(Mono.just(new TaskStatisticProjection()));
    }

    private GroupOperation constructTaskStatusGroupOperation() {
        return Aggregation.group()
                .count().as("total")
                .sum(ConditionalOperators.Cond.newBuilder()
                        .when(Criteria.where("status").is(TaskStatus.TODO))
                        .then(1)
                        .otherwise(0))
                .as("to_do_num")
                .sum(ConditionalOperators.Cond.newBuilder()
                        .when(Criteria.where("status").is(TaskStatus.IN_PROGRESS))
                        .then(1)
                        .otherwise(0))
                .as("in_progress_num")
                .sum(ConditionalOperators.Cond.newBuilder()
                        .when(Criteria.where("status").is(TaskStatus.IN_REVISION))
                        .then(1)
                        .otherwise(0))
                .as("in_revision_num")
                .sum(ConditionalOperators.Cond.newBuilder()
                        .when(Criteria.where("status").is(TaskStatus.DONE))
                        .then(1)
                        .otherwise(0))
                .as("done_num")
                .sum(ConditionalOperators.Cond.newBuilder()
                        .when(Criteria.where("status").is(TaskStatus.ARCHIVED))
                        .then(1)
                        .otherwise(0))
                .as("archived_num");
    }

    public Flux<String> getUserBoardIds(String userId) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("owner_id").is(userId),
                Criteria.where("joined_ids").in(userId)
        );
        Query query = Query.query(criteria);
        return template.find(query, BoardDocument.class)
                .map(BoardDocument::getAggregateId);
    }
}
