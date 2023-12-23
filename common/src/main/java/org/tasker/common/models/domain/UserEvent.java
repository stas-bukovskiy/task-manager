package org.tasker.common.models.domain;

import org.springframework.data.relational.core.mapping.Table;
import org.tasker.common.es.Event;

@Table("event_store")
public class UserEvent extends Event {
}
