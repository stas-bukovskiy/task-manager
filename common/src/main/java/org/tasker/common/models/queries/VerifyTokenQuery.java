package org.tasker.common.models.queries;

import lombok.Builder;

@Builder
public record VerifyTokenQuery(
        String token
) {
    public static final String QUERY_NAME = "verify_token";
}
