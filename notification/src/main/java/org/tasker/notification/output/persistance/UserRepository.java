package org.tasker.notification.output.persistance;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.tasker.common.models.domain.UserDocument;
import reactor.core.publisher.Mono;

@Repository("notificationUserRepository")
public interface UserRepository extends ReactiveMongoRepository<UserDocument, String> {
    Mono<UserDocument> findByAggregateId(String toUserId);
}
