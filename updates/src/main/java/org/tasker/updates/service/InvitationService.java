package org.tasker.updates.service;

import org.tasker.updates.models.request.ReviewInitiationRequest;
import reactor.core.publisher.Mono;

public interface InvitationService {
    Mono<Void> reviewInvitation(String currentUserId, ReviewInitiationRequest request);
}
