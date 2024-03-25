package org.tasker.notification.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tasker.common.models.domain.UserStatusDocument;
import org.tasker.notification.output.persistance.UserStatusRepository;
import org.tasker.notification.service.UserStatusService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Service("notificationUserStatusService")
public class UserStatusServiceImpl implements UserStatusService {

    private final UserStatusRepository userStatusRepository;

    public UserStatusServiceImpl(@Qualifier("notificationUserStatusRepository") UserStatusRepository userStatusRepository) {
        this.userStatusRepository = userStatusRepository;
    }

    @Override
    public Flux<String> filterByOnlineStatus(Set<String> assigneeIds, boolean isOnline) {
        return userStatusRepository.findByAggregateIdInAndOnline(assigneeIds, isOnline)
                .map(UserStatusDocument::getAggregateId);
    }

    @Override
    public Mono<Boolean> isUserOnline(String userId, boolean isOnline) {
        return userStatusRepository.findByAggregateIdAndOnline(userId, isOnline)
                .switchIfEmpty(Mono.just(Boolean.FALSE));
    }
}
