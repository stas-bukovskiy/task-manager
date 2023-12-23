package org.tasker.common.es;

import java.util.List;

public interface EventBus {
    void publish(List<Event> events);
}