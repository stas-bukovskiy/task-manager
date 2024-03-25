package org.tasker.notification.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tasker.common.exception.ItemNotFoundException;
import org.tasker.common.models.domain.UserDocument;
import org.tasker.notification.output.persistance.UserRepository;
import org.tasker.notification.service.UserService;
import org.tasker.notification.service.UserStatusService;
import reactor.core.publisher.Mono;

@Service("notificationUserService")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserStatusService userStatusService;

    public UserServiceImpl(@Qualifier("notificationUserRepository") UserRepository userRepository,
                           @Qualifier("notificationUserStatusService") UserStatusService userStatusService) {
        this.userRepository = userRepository;
        this.userStatusService = userStatusService;
    }

    @Override
    public Mono<UserDocument> getOnlineUser(String userId) {
        return userStatusService.isUserOnline(userId, true)
                .flatMap(isOnline -> isOnline ? getUser(userId) : Mono.empty());
    }

    @Override
    public Mono<UserDocument> getUser(String toUserId) {
        return userRepository.findByAggregateId(toUserId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("User %s not found", toUserId)));
    }
}
