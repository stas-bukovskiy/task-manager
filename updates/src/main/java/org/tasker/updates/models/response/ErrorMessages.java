package org.tasker.updates.models.response;


import org.springframework.http.HttpStatus;

import java.util.Map;

public final class ErrorMessages {

    public static final String INTERNAL_SERVER_ERROR = "Something went wrong, on our side. Please try again later or contact support.";
    public static final String UNKNOWN_ERROR = "Something went wrong, on our side. Please try again later or contact support.";
    public static final String BAD_WS_REQUEST = "Bed request";
    public static final Map<Integer, String> MAP = Map.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR
    );
}
