
# TODO: is naming good ?
ALTER TABLE repo ADD COLUMN cloned TIMESTAMP NULL;



CREATE TABLE metric_language
(
    language VARCHAR(256) NOT NULL PRIMARY KEY
);

CREATE TABLE repo_metrics
(
    repo_id BIGINT NOT NULL,
    metric_language_id VARCHAR(256) NOT NULL,

    lines_blank BIGINT DEFAULT 0,
    lines_code BIGINT DEFAULT 0,
    lines_comment BIGINT DEFAULT 0,

    CONSTRAINT unique_composite_key
        UNIQUE (repo_id, metric_language_id),
    FOREIGN KEY (repo_id) REFERENCES repo(id),
    FOREIGN KEY (metric_language_id) REFERENCES metric_language(language),
    PRIMARY KEY (repo_id, metric_language_id)
);