CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS events
(
    event_id       UUID                     DEFAULT uuid_generate_v4(),
    aggregate_id   VARCHAR(250) NOT NULL CHECK ( aggregate_id <> '' ),
    aggregate_type VARCHAR(250) NOT NULL CHECK ( aggregate_type <> '' ),
    event_type     VARCHAR(250) NOT NULL CHECK ( event_type <> '' ),
    data           BYTEA,
    metadata       BYTEA,
    version        SERIAL       NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (aggregate_id, version)
) PARTITION BY HASH (aggregate_id);

CREATE INDEX IF NOT EXISTS aggregate_id_aggregate_version_idx ON events USING btree (aggregate_id, version ASC);

CREATE TABLE IF NOT EXISTS events_partition_hash_1 PARTITION OF events
    FOR VALUES WITH (MODULUS 3, REMAINDER 0);

CREATE TABLE IF NOT EXISTS events_partition_hash_2 PARTITION OF events
    FOR VALUES WITH (MODULUS 3, REMAINDER 1);

CREATE TABLE IF NOT EXISTS events_partition_hash_3 PARTITION OF events
    FOR VALUES WITH (MODULUS 3, REMAINDER 2);

CREATE TABLE IF NOT EXISTS snapshots
(
    snapshot_id    UUID PRIMARY KEY         DEFAULT uuid_generate_v4(),
    aggregate_id   VARCHAR(250) UNIQUE NOT NULL CHECK ( aggregate_id <> '' ),
    aggregate_type VARCHAR(250)        NOT NULL CHECK ( aggregate_type <> '' ),
    data           BYTEA,
    metadata       BYTEA,
    version        SERIAL              NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (aggregate_id)
);

CREATE INDEX IF NOT EXISTS aggregate_id_aggregate_version_idx ON snapshots USING btree (aggregate_id, version);

-- create reservation tables
CREATE TABLE IF NOT EXISTS username_email_reservation
(
    aggregate_id VARCHAR(255) PRIMARY KEY,
    email        VARCHAR(255) UNIQUE,
    username     VARCHAR(255) UNIQUE
);