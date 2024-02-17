package org.tasker.updates.service;

import org.tasker.common.es.Event;
import org.tasker.updates.models.response.NotificationResponse;

public interface NotificationService {
    NotificationResponse<?> processEvent(Event event);
}
