package org.tasker.auth.output.persistance;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import org.tasker.auth.models.domain.UsernameEmailReservation;
import reactor.core.publisher.Mono;

@Repository
public interface UserReservationRepository extends R2dbcRepository<UsernameEmailReservation, String> {
    Mono<Boolean> existsByUsernameOrEmail(String email, String username);

    Mono<Boolean> existsByUsernameAndAggregateIdIsNot(String username, String aggregateID);

    @Modifying
    @Query("update username_email_reservation set username = :username where aggregate_id = :aggregateId")
    Mono<Void> updateUsername(String aggregateId, String username);
}
