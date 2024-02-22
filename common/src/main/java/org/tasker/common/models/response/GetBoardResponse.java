package org.tasker.common.models.response;

import org.tasker.common.models.dto.BoardDto;

import java.util.List;

public class GetBoardResponse extends Response<List<BoardDto>> {
    GetBoardResponse(int httpCode, String message, List<BoardDto> data) {
        super(httpCode, message, data);
    }
}
