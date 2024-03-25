package org.tasker.common.models.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Set;

@Data
@Builder
@Document(collection = "users")
public class UserDocument {

    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @Field("processed_events")
    Set<String> processedEvents;

    @Indexed
    private String username;

    @Indexed
    private String email;

    private String password;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;
    @Field("aggregate_id")
    private String aggregateId;

}
