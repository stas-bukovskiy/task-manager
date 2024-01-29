package org.tasker.common.es;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Snapshot {

    private UUID id;
    private String aggregateId;
    private String aggregateType;
    private byte[] data;
    private byte[] metaData;
    private long version;
    private LocalDateTime timeStamp;

}