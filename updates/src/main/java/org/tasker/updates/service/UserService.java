package org.tasker.updates.service;

import org.tasker.common.models.dto.UserDto;
import org.tasker.common.models.response.DefaultResponse;
import org.tasker.updates.models.request.UpdateUserInfoRequest;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserService {
    Mono<UserDto> getUserByAggregateId(String aggregateId);

    Mono<DefaultResponse> updateUserInfo(String userId, UpdateUserInfoRequest data);

    Mono<List<UserDto>> searchPeople(String search);

}
