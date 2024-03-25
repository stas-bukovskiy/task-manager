package org.tasker.notification.models.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

@Data
@Builder
@Document(collection = "notifications")
public class NotificationDocument {

    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @Indexed(unique = true)
    @Field("aggregate_id")
    private String aggregateId;

    @Field("user_id")
    private String userId;

    private String message;

    @Field("for_aggregate_type")
    private String forAggregateType;

    @Field("for_aggregate_id")
    private String forAggregateId;

    @Field("valid")
    private boolean valid;

    @Field("deleted")
    private boolean deleted;

    @Field("created_at")
    private Date createdAt;

}
