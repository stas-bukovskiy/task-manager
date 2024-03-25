package org.tasker.updates.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.models.domain.UserStatusDocument;
import org.tasker.updates.persistance.UserStatusRepository;
import org.tasker.updates.service.UserStatusService;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service("updatesUserStatusService")
public class UserStatusServiceImpl implements UserStatusService {

    private final UserStatusRepository userStatusRepository;

    @Override
    public Mono<Void> updateUserStatus(String currentUserId, boolean isOnline) {
        return userStatusRepository.save(new UserStatusDocument(currentUserId, isOnline))
                .doOnSuccess(userStatusDocument -> log.info("User status updated: {}", userStatusDocument))
                .then();
    }
}
