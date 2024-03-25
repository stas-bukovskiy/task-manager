package org.tasker.notification.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface UserStatusService {
    Flux<String> filterByOnlineStatus(Set<String> assigneeIds, boolean isOnline);

    Mono<Boolean> isUserOnline(String userId, boolean isOnline);
}
