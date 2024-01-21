package org.tasker.common.models.response;


import org.tasker.common.models.dto.UserDto;

import java.util.List;

public class UsersResponse extends Response<List<UserDto>> {

    UsersResponse(int httpCode, String message, List<UserDto> data) {
        super(httpCode, message, data);
    }
}
