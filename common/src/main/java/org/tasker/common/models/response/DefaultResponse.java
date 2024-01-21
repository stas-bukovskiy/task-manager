package org.tasker.common.models.response;


public class DefaultResponse extends Response<String> {

    DefaultResponse(int httpCode, String message, String data) {
        super(httpCode, message, data);
    }
}
