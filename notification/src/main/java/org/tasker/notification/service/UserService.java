package org.tasker.notification.service;

import org.tasker.common.models.domain.UserDocument;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserDocument> getOnlineUser(String userId);

    Mono<UserDocument> getUser(String toUserId);
}
