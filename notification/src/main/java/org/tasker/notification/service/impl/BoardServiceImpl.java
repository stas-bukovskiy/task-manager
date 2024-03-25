package org.tasker.notification.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tasker.common.exception.ItemNotFoundException;
import org.tasker.common.models.domain.BoardDocument;
import org.tasker.notification.output.persistance.BoardRepository;
import org.tasker.notification.service.BoardService;
import org.tasker.notification.service.UserStatusService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service("notificationBoardService")
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final UserStatusService userStatusService;

    public BoardServiceImpl(@Qualifier("notificationBoardRepository") BoardRepository boardRepository,
                            @Qualifier("notificationUserStatusService") UserStatusService userStatusService) {
        this.boardRepository = boardRepository;
        this.userStatusService = userStatusService;
    }

    @Override
    public Flux<String> getBoardOnlineUserIds(String aggregateId) {
        return boardRepository.findByAggregateId(aggregateId)
                .map(board -> {
                    Set<String> joinedIds = new HashSet<>(board.getJoinedIds() != null ? board.getJoinedIds() : Collections.emptySet());
                    joinedIds.add(board.getOwnerId());

                    return joinedIds;
                })
                .flatMapMany(userIds -> userStatusService.filterByOnlineStatus(userIds, true));
    }

    @Override
    public Mono<BoardDocument> getBoard(String aggregateId) {
        return boardRepository.findByAggregateId(aggregateId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException("Board %s not found", aggregateId)));
    }
}
