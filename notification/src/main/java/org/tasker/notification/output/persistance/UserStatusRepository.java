package org.tasker.notification.output.persistance;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.tasker.common.models.domain.UserStatusDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Repository("notificationUserStatusRepository")
public interface UserStatusRepository extends ReactiveMongoRepository<UserStatusDocument, String> {
    Flux<UserStatusDocument> findByAggregateIdInAndOnline(Set<String> aggregateIds, boolean online);

    Mono<Boolean> findByAggregateIdAndOnline(String aggregateId, boolean online);
}
