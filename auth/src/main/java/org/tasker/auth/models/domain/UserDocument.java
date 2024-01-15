package org.tasker.auth.models.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Field("processed_events")
    Set<String> processedEvents;
    @MongoId(FieldType.OBJECT_ID)
    private String id;
    @JsonProperty("aggregate_id")
    private String aggregateId;
    @Indexed
    private String username;
    @Indexed
    private String email;
    private String password;
    @Field("first_name")
    private String firstName;
    @Field("last_name")
    private String lastName;

}
