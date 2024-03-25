package org.tasker.notification.output.persistance;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.tasker.notification.models.domain.NotificationDocument;
import reactor.core.publisher.Mono;

public interface NotificationRepository extends ReactiveMongoRepository<NotificationDocument, String> {
    Mono<NotificationDocument> findByUserIdAndForAggregateIdAndForAggregateTypeAndValid(String userId, String fotAggId, String type, boolean valid);

    Mono<Boolean> existsByAggregateId(String aggregateId);

    Mono<NotificationDocument> findByAggregateId(String aggregateId);

    Mono<Void> deleteByAggregateId(String aggregateId);
}
