package org.tasker.common.models.dto;


import lombok.Builder;

@Builder
public record LoginData(
        String token,
        UserDto user
) {

}
