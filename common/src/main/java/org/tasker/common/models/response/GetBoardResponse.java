package org.tasker.common.models.response;

import org.tasker.common.models.dto.BoardDto;

public class GetBoardResponse extends Response<BoardDto> {
    GetBoardResponse(int httpCode, String message, BoardDto data) {
        super(httpCode, message, data);
    }
}
