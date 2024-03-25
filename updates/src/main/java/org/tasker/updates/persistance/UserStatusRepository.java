package org.tasker.updates.persistance;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.tasker.common.models.domain.UserStatusDocument;


@Repository
public interface UserStatusRepository extends ReactiveMongoRepository<UserStatusDocument, String> {
}
