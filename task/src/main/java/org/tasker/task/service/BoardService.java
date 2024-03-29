package org.tasker.task.service;

import org.tasker.common.models.commands.CreateBoardCommand;
import org.tasker.common.models.dto.BoardDto;
import org.tasker.common.models.dto.BoardStatistic;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BoardService {
    Mono<BoardStatistic> getStatistic(String userAggregateId);

    Mono<Void> createBoard(CreateBoardCommand command);

    Mono<List<BoardDto>> getBoards(String userId);

    Mono<BoardDto> getBoard(String userId, String boardId);

    Mono<Void> deleteBoard(String boardId, String userId);

    Mono<Void> deleteMember(String boardId, String userId, String memberId);

    Mono<Void> updateBoard(String boardId, String userId, String title);
}
