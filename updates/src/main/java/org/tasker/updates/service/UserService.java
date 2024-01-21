package org.tasker.updates.service;

import org.tasker.common.models.dto.UserDto;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserDto> getUserByAggregateId(String aggregateId);
}
