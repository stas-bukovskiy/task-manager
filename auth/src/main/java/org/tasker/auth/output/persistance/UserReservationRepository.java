package org.tasker.auth.output.persistance;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.tasker.auth.models.domain.UsernameEmailReservation;
import reactor.core.publisher.Mono;

@Repository
public interface UserReservationRepository extends ReactiveCrudRepository<UsernameEmailReservation, String> {
    Mono<Boolean> existsByUsernameOrEmail(String email, String username);

    Mono<Boolean> existsByUsernameAndAggregateIdIsNot(String username, String aggregateID);
}
