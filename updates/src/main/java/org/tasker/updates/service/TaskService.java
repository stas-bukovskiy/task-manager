package org.tasker.updates.service;

import org.tasker.common.models.response.*;
import org.tasker.updates.models.request.*;
import reactor.core.publisher.Mono;

public interface TaskService {
    Mono<GetStatisticResponse> getUserStatistic(String userAggregateId);

    Mono<Void> createBoard(String currentUserId, CreateBoardRequest request);

    Mono<GetBoardsResponse> getBoards(String currentUserId);

    Mono<GetBoardResponse> getBoard(String currentUserId, String boardId);

    Mono<Void> updateBoard(String currentUserId, UpdateBoardRequest request);

    Mono<Void> deleteMember(String currentUserId, DeleteMemberRequest request);

    Mono<Void> deleteBoard(String currentUserId, DeleteBoardRequest request);

    Mono<GetTasksResponse> getTasks(String currentUserId, GetTasksRequest request);

    Mono<GetTaskResponse> getTask(String currentUserId, GetTaskRequest request);

    Mono<Void> createTask(String currentUserId, CreateTaskRequest request);

    Mono<Void> updateTask(String currentUserId, UpdateTaskInfoRequest request);

    Mono<Void> updateTaskStatus(String currentUserId, UpdateTaskStatusRequest request);

    Mono<Void> addAssignee(String currentUserId, AddAssigneeRequest request);

    Mono<Void> deleteAssignee(String currentUserId, DeleteAssigneeRequest request);

    Mono<Void> deleteTask(String currentUserId, DeleteTaskRequest request);
}
