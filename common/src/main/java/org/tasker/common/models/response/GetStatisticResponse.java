package org.tasker.common.models.response;

import org.tasker.common.models.dto.Statistic;

public class GetStatisticResponse extends Response<Statistic> {

    public GetStatisticResponse(int httpCode, String message, Statistic data) {
        super(httpCode, message, data);
    }
}
