package org.tasker.updates.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.commands.UpdateUserCommand;
import org.tasker.common.models.dto.UserDto;
import org.tasker.common.models.queries.GetUserQuery;
import org.tasker.common.models.response.DefaultResponse;
import org.tasker.common.models.response.UsersResponse;
import org.tasker.updates.exceptions.ItemNotFountException;
import org.tasker.updates.models.request.UpdateUserInfoRequest;
import org.tasker.updates.output.event.AuthPublisher;
import org.tasker.updates.service.UserService;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AuthPublisher publisher;

    @Override
    public Mono<UserDto> getUserByAggregateId(String aggregateId) {
        return publisher.publishAndReceive(
                        GetUserQuery.QUERY_NAME,
                        GetUserQuery.builder()
                                .aggregateId(aggregateId)
                                .build()
                )
                .map(responseBytes -> SerializerUtils.deserializeFromJsonBytes(responseBytes, UsersResponse.class))
                .handle((response, sink) -> {
                    if (response.getData().isEmpty()) {
                        sink.error(new ItemNotFountException("User with such ID " + aggregateId + " not found"));
                    } else {
                        sink.next(response.getData().get(0));
                    }
                });
    }

    @Override
    public Mono<DefaultResponse> updateUserInfo(String userId, UpdateUserInfoRequest data) {
        return publisher.publishAndReceive(
                        UpdateUserCommand.COMMAND_NAME,
                        UpdateUserCommand.builder()
                                .aggregateID(userId)
                                .username(data.username())
                                .firstName(data.firstName())
                                .lastName(data.lastName())
                                .build()
                )
                .map(responseBytes -> SerializerUtils.deserializeFromJsonBytes(responseBytes, DefaultResponse.class));
    }
}
