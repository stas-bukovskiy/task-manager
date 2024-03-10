package org.tasker.updates.service;

import org.tasker.updates.models.request.DeleteInvitationRequest;
import org.tasker.updates.models.request.InviteUsersRequest;
import org.tasker.updates.models.request.ReviewInitiationRequest;
import reactor.core.publisher.Mono;

public interface InvitationService {
    Mono<Void> reviewInvitation(String currentUserId, ReviewInitiationRequest request);

    Mono<Void> inviteUser(String currentUserId, InviteUsersRequest request);

    Mono<Void> deleteInvitation(String currentUserId, DeleteInvitationRequest request);
}
