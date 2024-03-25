package org.tasker.notification.output.persistance;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.tasker.common.models.domain.TaskDocument;

@Repository("notificationTaskRepository")
public interface TaskRepository extends ReactiveMongoRepository<TaskDocument, String>, CustomTaskRepository {
}
