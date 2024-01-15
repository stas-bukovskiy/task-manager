package org.tasker.auth.output.persistance;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.tasker.auth.models.domain.UserDocument;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<UserDocument, String> {
    Mono<Void> deleteByAggregateId(String aggregateId);

    Mono<UserDocument> findByAggregateId(String aggregateId);

    Mono<UserDocument> findByEmailOrUsername(String email, String username);
}
