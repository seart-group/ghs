
# TODO: is naming good ?
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
    FOREIGN KEY (repo_id) REFERENCES repo(id),
    FOREIGN KEY (metric_language_id) REFERENCES metric_language(id),
    PRIMARY KEY (repo_id, metric_language_id)
);

#
# DROP TABLE repo_metrics, metric_language;
# ALTER TABLE repo DROP COLUMN cloned;

# UPDATE repo SET cloned=null WHERE TRUE;
# DELETE FROM repo_metrics WHERE TRUE;
# DELETE FROM metric_language WHERE TRUE;