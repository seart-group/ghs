CREATE TABLE event_log (
    name VARCHAR(255) NOT NULL,
    start TIMESTAMP NOT NULL,
    end TIMESTAMP NOT NULL,
    CONSTRAINT unique_composite_key
        UNIQUE (name, start)
);

ALTER TABLE event_log ADD CHECK (start <= end);


