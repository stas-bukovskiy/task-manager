package org.tasker.updates.service;

import reactor.core.publisher.Mono;

public interface UserStatusService {
    Mono<Void> updateUserStatus(String currentUserId, boolean isOnline);
}
