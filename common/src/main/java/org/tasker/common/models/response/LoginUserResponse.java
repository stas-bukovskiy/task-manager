package org.tasker.common.models.response;


import org.tasker.common.models.dto.LoginData;

public class LoginUserResponse extends Response<LoginData> {

    LoginUserResponse(int httpCode, String message, LoginData data) {
        super(httpCode, message, data);
    }
}
