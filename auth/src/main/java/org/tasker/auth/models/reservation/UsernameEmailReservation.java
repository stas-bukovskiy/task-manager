package org.tasker.auth.models.reservation;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("username_email_reservation")
@Data
@Builder
public class UsernameEmailReservation {

    @Column("aggregate_id")
    private String aggregateId;

    @Column("email")
    private String email;

    @Column("username")
    private String username;

}
