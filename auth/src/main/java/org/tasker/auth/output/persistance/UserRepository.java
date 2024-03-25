package org.tasker.auth.output.persistance;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.tasker.common.models.domain.UserDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<UserDocument, String> {
    Mono<Void> deleteByAggregateId(String aggregateId);

    Mono<UserDocument> findByAggregateId(String aggregateId);

    Mono<UserDocument> findByEmailOrUsername(String email, String username);

    @Query("{'$or': [{'first_mame': {'$regex': ?0, '$options': 'i'}}, {'last_mame': {'$regex': ?0, '$options': 'i'}}, {'username': {'$regex': ?0, '$options': 'i'}}, {'email': {'$regex': ?0, '$options': 'i'}}]}")
    Flux<UserDocument> findAllBySearching(String search);
}
