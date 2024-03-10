package org.tasker.updates.service;

import org.tasker.common.models.response.GetBoardResponse;
import org.tasker.common.models.response.GetBoardsResponse;
import org.tasker.common.models.response.GetStatisticResponse;
import org.tasker.updates.models.request.CreateBoardRequest;
import org.tasker.updates.models.request.DeleteBoardRequest;
import org.tasker.updates.models.request.DeleteMemberRequest;
import org.tasker.updates.models.request.UpdateBoardRequest;
import reactor.core.publisher.Mono;

public interface TaskService {
    Mono<GetStatisticResponse> getUserStatistic(String userAggregateId);

    Mono<Void> createBoard(String currentUserId, CreateBoardRequest request);

    Mono<GetBoardsResponse> getBoards(String currentUserId);

    Mono<GetBoardResponse> getBoard(String currentUserId, String s);

    Mono<Void> updateBoard(String currentUserId, UpdateBoardRequest request);

    Mono<Void> deleteMember(String currentUserId, DeleteMemberRequest request);

    Mono<Void> deleteBoard(String currentUserId, DeleteBoardRequest request);
}
