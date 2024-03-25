package org.tasker.notification.service;

import org.tasker.common.es.Event;
import org.tasker.notification.models.domain.NotificationDocument;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Void> processEvent(Event event);

    Mono<NotificationDocument> getNotification(String userId, String fotAggId, String type);

    Mono<NotificationDocument> getNotification(String aggId);

    Mono<Void> invalidateNotification(String userId, String fotAggId, String type);
}
