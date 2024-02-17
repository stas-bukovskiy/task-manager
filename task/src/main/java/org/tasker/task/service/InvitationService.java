package org.tasker.task.service;

import org.tasker.common.models.commands.InviteUsersCommand;
import reactor.core.publisher.Mono;

public interface InvitationService {
    Mono<Void> inviteUsersToBoard(InviteUsersCommand command);
}
