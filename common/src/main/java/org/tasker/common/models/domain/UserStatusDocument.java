package org.tasker.common.models.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@AllArgsConstructor
@Document("users_statuses")
public class UserStatusDocument {

    @MongoId(FieldType.STRING)
    @Field("aggregate_id")
    private String aggregateId;

    private boolean online;

}
