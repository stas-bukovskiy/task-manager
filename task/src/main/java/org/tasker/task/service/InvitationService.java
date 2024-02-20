package org.tasker.task.service;

import org.tasker.common.models.commands.InviteUsersCommand;
import org.tasker.common.models.commands.ReviewInvitationCommand;
import reactor.core.publisher.Mono;

public interface InvitationService {
    Mono<Void> inviteUsersToBoard(InviteUsersCommand command);

    Mono<Void> reviewInvitation(ReviewInvitationCommand command);
}
