package org.tasker.common.models.response;

import org.tasker.common.models.dto.TaskDto;

public class GetTaskResponse extends Response<TaskDto> {
    GetTaskResponse(int httpCode, String message, TaskDto data) {
        super(httpCode, message, data);
    }
}
