package org.tasker.common.models.response;

import org.tasker.common.models.dto.TaskDto;

import java.util.List;

public class GetTasksResponse extends Response<List<TaskDto>> {
    GetTasksResponse(int httpCode, String message, List<TaskDto> data) {
        super(httpCode, message, data);
    }
}
