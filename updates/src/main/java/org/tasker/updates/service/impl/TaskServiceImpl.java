package org.tasker.updates.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.commands.CreateBoardCommand;
import org.tasker.common.models.queries.GetBoardQuery;
import org.tasker.common.models.queries.GetBoardsQuery;
import org.tasker.common.models.queries.GetStatisticQuery;
import org.tasker.common.models.response.GetBoardResponse;
import org.tasker.common.models.response.GetBoardsResponse;
import org.tasker.common.models.response.GetStatisticResponse;
import org.tasker.updates.models.request.CreateBoardRequest;
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
}
