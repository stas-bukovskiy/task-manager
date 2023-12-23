package org.tasker.auth.input.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tasker.auth.service.AuthCommandService;
import org.tasker.common.models.command.RegisterNewUserCommand;

@RequiredArgsConstructor
@Component
public class UserCommandConsumer {

    private final AuthCommandService authCommandService;

    public String handleRegisterNewUser(RegisterNewUserCommand request) {
        return null;
    }
}
