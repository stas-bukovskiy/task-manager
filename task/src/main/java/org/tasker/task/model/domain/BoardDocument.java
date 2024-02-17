package org.tasker.task.model.domain;

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
@Document(collection = "boards")
public class BoardDocument {

    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @Indexed(unique = true)
    @Field("aggregate_id")
    private String aggregateId;

    private String title;

    @Indexed
    @Field("owner_id")
    private String ownerId;

    @Field("invited_ids")
    private Set<String> invitedIds;

    @Field("joined_ids")
    private Set<String> joinedIds;

    @Field("processed_events")
    private Set<String> processedEvents;

}
