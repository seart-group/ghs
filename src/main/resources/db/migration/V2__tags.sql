

CREATE TABLE tags
(
    id BIGINT NOT NULL PRIMARY KEY,
    label VARCHAR(256) NOT NULL UNIQUE
);

CREATE TABLE repo_tags
(
    repo_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,

    CONSTRAINT unique_composite_key
        UNIQUE (repo_id, tag_id),
    FOREIGN KEY (repo_id) REFERENCES repo(id),
    FOREIGN KEY (tag_id) REFERENCES tags(id),
    PRIMARY KEY (repo_id, tag_id)
);

CREATE INDEX repo_tags_repo_id_index ON repo_tags(repo_id, tag_id);