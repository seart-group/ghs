ALTER TABLE repo ADD COLUMN cloned TIMESTAMP NULL;


CREATE TABLE metric_language
(
    id BIGINT NOT NULL PRIMARY KEY,
    language VARCHAR(256) NOT NULL UNIQUE
);

CREATE TABLE repo_metrics
(
    repo_id BIGINT NOT NULL,
    metric_language_id BIGINT NOT NULL,

    lines_blank BIGINT DEFAULT 0,
    lines_code BIGINT DEFAULT 0,
    lines_comment BIGINT DEFAULT 0,

    CONSTRAINT unique_composite_key
        UNIQUE (repo_id, metric_language_id),
    FOREIGN KEY (repo_id) REFERENCES repo(id) ON DELETE CASCADE,
    FOREIGN KEY (metric_language_id) REFERENCES metric_language(id) ON DELETE CASCADE,
    PRIMARY KEY (repo_id, metric_language_id)
);

create index repo_metrics_repo_id_metric_language_id_index
    on repo_metrics (repo_id, metric_language_id);