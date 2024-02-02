package org.tasker.task.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.models.dto.BoardStatistic;
import org.tasker.task.service.BoardService;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    @Override
    public Mono<BoardStatistic> getStatistic(String userAggregateId) {
        // TODO: implement when board service is ready
        return Mono.just(BoardStatistic.builder()
                .createdNum(1)
                .joinedNum(1)
                .build());
    }
}
