package org.tasker.updates.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.commands.*;
import org.tasker.common.models.enums.TaskPriority;
import org.tasker.common.models.enums.TaskStatus;
import org.tasker.common.models.queries.*;
import org.tasker.common.models.response.*;
import org.tasker.updates.models.request.*;
import org.tasker.updates.output.event.TaskCommunicator;
import org.tasker.updates.service.TaskService;
import reactor.core.publisher.Mono;

@Service("updatesTaskService")
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskCommunicator communicator;

    @Override
    public Mono<GetStatisticResponse> getUserStatistic(String userAggregateId) {
        return communicator.publishAndReceive(
                GetStatisticQuery.QUERY_NAME,
                GetStatisticQuery.builder()
                        .userAggregateId(userAggregateId)
                        .build()
        ).map(bytes -> SerializerUtils.deserializeFromJsonBytes(bytes, GetStatisticResponse.class));
    }

    @Override
    public Mono<Void> createBoard(String currentUserId, CreateBoardRequest request) {
        return communicator.publish(
                CreateBoardCommand.COMMAND_NAME,
                CreateBoardCommand.builder()
                        .title(request.title())
                        .ownerId(currentUserId)
                        .invitedUserIds(request.invitedUserIds())
                        .build()
        );
    }

    @Override
    public Mono<GetBoardsResponse> getBoards(String currentUserId) {
        return communicator.publishAndReceive(
                GetBoardsQuery.QUERY_NAME,
                GetBoardsQuery.builder()
                        .userId(currentUserId)
                        .build()
        ).map(bytes -> SerializerUtils.deserializeFromJsonBytes(bytes, GetBoardsResponse.class));
    }

    @Override
    public Mono<GetBoardResponse> getBoard(String currentUserId, String boardId) {
        return communicator.publishAndReceive(
                GetBoardQuery.QUERY_NAME,
                GetBoardQuery.builder()
                        .userId(currentUserId)
                        .boardId(boardId)
                        .build()
        ).map(bytes -> SerializerUtils.deserializeFromJsonBytes(bytes, GetBoardResponse.class));
    }

    @Override
    public Mono<Void> updateBoard(String currentUserId, UpdateBoardRequest request) {
        return communicator.publish(
                UpdateBoardCommand.COMMAND_NAME,
                UpdateBoardCommand.builder()
                        .boardId(request.boardId())
                        .title(request.title())
                        .userId(currentUserId)
                        .build()
        );
    }

    @Override
    public Mono<Void> deleteMember(String currentUserId, DeleteMemberRequest request) {
        return communicator.publish(
                DeleteMemberCommand.COMMAND_NAME,
                DeleteMemberCommand.builder()
                        .boardId(request.boardId())
                        .userId(currentUserId)
                        .memberId(request.userId())
                        .build()
        );
    }

    @Override
    public Mono<Void> deleteBoard(String currentUserId, DeleteBoardRequest request) {
        return communicator.publish(
                DeleteBoardCommand.COMMAND_NAME,
                DeleteBoardCommand.builder()
                        .boardId(request.boardId())
                        .userId(currentUserId)
                        .build()
        );
    }

    @Override
    public Mono<GetTasksResponse> getTasks(String currentUserId, GetTasksRequest request) {
        return communicator.publishAndReceive(
                GetTasksQuery.QUERY_NAME,
                GetTasksQuery.builder()
                        .userId(currentUserId)
                        .boardId(request.boardId())
                        .build()
        ).map(bytes -> SerializerUtils.deserializeFromJsonBytes(bytes, GetTasksResponse.class));
    }

    @Override
    public Mono<GetTaskResponse> getTask(String currentUserId, GetTaskRequest request) {
        return communicator.publishAndReceive(
                GetTaskQuery.QUERY_NAME,
                GetTaskQuery.builder()
                        .userId(currentUserId)
                        .taskId(request.taskId())
                        .build()
        ).map(bytes -> SerializerUtils.deserializeFromJsonBytes(bytes, GetTaskResponse.class));
    }

    @Override
    public Mono<Void> createTask(String currentUserId, CreateTaskRequest request) {
        return communicator.publish(
                CreateTaskCommand.COMMAND_NAME,
                CreateTaskCommand.builder()
                        .boardId(request.boardId())
                        .userId(currentUserId)
                        .title(request.title())
                        .description(request.description())
                        .startDate(request.startDate())
                        .dueDate(request.dueDate())
                        .estimatedTime(request.estimatedTime())
                        .priority(TaskPriority.valueOf(request.priority()))
                        .status(TaskStatus.valueOf(request.status()))
                        .assigneeIds(request.assigneeIds())
                        .build()
        );
    }

    @Override
    public Mono<Void> updateTask(String currentUserId, UpdateTaskInfoRequest request) {
        return communicator.publish(
                UpdateTaskInfoCommand.COMMAND_NAME,
                UpdateTaskInfoCommand.builder()
                        .taskId(request.taskId())
                        .userId(currentUserId)
                        .title(request.title())
                        .description(request.description())
                        .startDate(request.startDate())
                        .dueDate(request.dueDate())
                        .estimatedTime(request.estimatedTime())
                        .priority(TaskPriority.valueOf(request.priority()))
                        .build()
        );
    }

    @Override
    public Mono<Void> updateTaskStatus(String currentUserId, UpdateTaskStatusRequest request) {
        return communicator.publish(
                UpdateTaskStatusCommand.COMMAND_NAME,
                UpdateTaskStatusCommand.builder()
                        .taskId(request.taskId())
                        .userId(currentUserId)
                        .status(TaskStatus.valueOf(request.status()))
                        .build()
        );
    }

    @Override
    public Mono<Void> addAssignee(String currentUserId, AddAssigneeRequest request) {
        return communicator.publish(
                AddAssigneeCommand.COMMAND_NAME,
                AddAssigneeCommand.builder()
                        .taskId(request.taskId())
                        .userId(currentUserId)
                        .assigneeId(request.assigneeId())
                        .build()
        );
    }

    @Override
    public Mono<Void> deleteAssignee(String currentUserId, DeleteAssigneeRequest request) {
        return communicator.publish(
                DeleteAssigneeCommand.COMMAND_NAME,
                DeleteAssigneeCommand.builder()
                        .taskId(request.taskId())
                        .userId(currentUserId)
                        .assigneeId(request.assigneeId())
                        .build()
        );
    }

    @Override
    public Mono<Void> deleteTask(String currentUserId, DeleteTaskRequest request) {
        return communicator.publish(
                DeleteTaskCommand.COMMAND_NAME,
                DeleteTaskCommand.builder()
                        .taskId(request.taskId())
                        .userId(currentUserId)
                        .build()
        );
    }
}
