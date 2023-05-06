
CREATE TABLE topics
(
    id BIGINT NOT NULL PRIMARY KEY,
    label VARCHAR(256) NOT NULL UNIQUE
);

CREATE TABLE repo_topics
(
    repo_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,

    CONSTRAINT unique_composite_key
        UNIQUE (repo_id, topic_id),
    FOREIGN KEY (repo_id) REFERENCES repo(id),
    FOREIGN KEY (topic_id) REFERENCES topics(id),
    PRIMARY KEY (repo_id, topic_id)
);

CREATE INDEX repo_tags_repo_id_index ON repo_topics(repo_id, topic_id);