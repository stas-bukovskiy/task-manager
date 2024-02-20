package org.tasker.updates.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.models.commands.ReviewInvitationCommand;
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
                        .invitationId(request.invitationId())
                        .userId(currentUserId)
                        .build()
        );
    }

}
