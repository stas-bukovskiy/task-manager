package org.tasker.common.es;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventBusMockImpl implements EventBus {
    @Override
    public void publish(List<Event> events) {

    }
}
