package org.tasker.notification.service;

import org.tasker.common.es.Event;
import org.tasker.common.models.dto.UpdateDto;
import reactor.core.publisher.Flux;

public interface UpdateService {
    Flux<UpdateDto<?>> processEvent(Event event);
}
