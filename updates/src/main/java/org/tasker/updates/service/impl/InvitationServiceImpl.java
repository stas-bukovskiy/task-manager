package org.tasker.updates.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.models.commands.DeleteInvitationCommand;
import org.tasker.common.models.commands.InviteUsersCommand;
import org.tasker.common.models.commands.ReviewInvitationCommand;
import org.tasker.updates.models.request.DeleteInvitationRequest;
import org.tasker.updates.models.request.InviteUsersRequest;
import org.tasker.updates.models.request.ReviewInitiationRequest;
import org.tasker.updates.output.event.TaskCommunicator;
import org.tasker.updates.service.InvitationService;
import reactor.core.publisher.Mono;

@Slf4j
@Service("updatesInvitationService")
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final TaskCommunicator communicator;

    @Override
    public Mono<Void> reviewInvitation(String currentUserId, ReviewInitiationRequest request) {
        return communicator.publish(
                ReviewInvitationCommand.COMMAND_NAME,
                ReviewInvitationCommand.builder()
                        .isAccepted(request.isAccepted())
                        .boardId(request.boardId())
                        .userId(currentUserId)
                        .build()
        );
    }

    @Override
    public Mono<Void> inviteUser(String currentUserId, InviteUsersRequest request) {
        return communicator.publish(
                InviteUsersCommand.COMMAND_NAME,
                InviteUsersCommand.builder()
                        .boardId(request.boardId())
                        .fromUserId(currentUserId)
                        .toUserIds(request.toUserIds())
                        .build()
        );
    }

    @Override
    public Mono<Void> deleteInvitation(String currentUserId, DeleteInvitationRequest request) {
        return communicator.publish(
                DeleteInvitationCommand.COMMAND_NAME,
                DeleteInvitationCommand.builder()
                        .boardId(request.boardId())
                        .userId(request.userId())
                        .ownerId(currentUserId)
                        .build()
        );
    }

}
