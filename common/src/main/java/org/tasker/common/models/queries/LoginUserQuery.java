package org.tasker.common.models.queries;

import lombok.Builder;

@Builder
public record LoginUserQuery(
        String login,
        String password
) {
    public static final String QUERY_NAME = "login";
}
