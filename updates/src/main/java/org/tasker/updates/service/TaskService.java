package org.tasker.updates.service;

import org.tasker.common.models.response.GetBoardResponse;
import org.tasker.common.models.response.GetBoardsResponse;
import org.tasker.common.models.response.GetStatisticResponse;
import org.tasker.updates.models.request.CreateBoardRequest;
import reactor.core.publisher.Mono;

public interface TaskService {
    Mono<GetStatisticResponse> getUserStatistic(String userAggregateId);

    Mono<Void> createBoard(String currentUserId, CreateBoardRequest request);

    Mono<GetBoardsResponse> getBoards(String currentUserId);

    Mono<GetBoardResponse> getBoard(String currentUserId, String s);
}
