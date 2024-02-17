package org.tasker.updates.service;

import org.tasker.common.es.Event;
import org.tasker.updates.models.response.UpdateResponse;

import java.util.List;

public interface UpdateService {
    List<UpdateResponse<?>> processEvent(Event event);
}
