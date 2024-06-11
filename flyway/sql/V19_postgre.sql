-- HIBERNATE SEQUENCE
-- TODO: Fix this to select the current max id from the table

CREATE SEQUENCE hibernate_sequence START WITH 95070998 INCREMENT BY 1;

-- TABLE COLUMN DEFAULTS

ALTER TABLE git_repo
    ALTER COLUMN name TYPE TEXT,
    ALTER COLUMN default_branch TYPE TEXT,
    ALTER COLUMN homepage TYPE TEXT,
    ALTER COLUMN last_commit_sha TYPE TEXT;

ALTER TABLE git_repo_metric
    ALTER COLUMN lines_blank SET DEFAULT 0,
    ALTER COLUMN lines_code SET DEFAULT 0,
    ALTER COLUMN lines_comment SET DEFAULT 0;

ALTER TABLE label ALTER COLUMN name TYPE TEXT;
ALTER TABLE language ALTER COLUMN name TYPE TEXT;
ALTER TABLE license ALTER COLUMN name TYPE TEXT;
ALTER TABLE topic ALTER COLUMN name TYPE TEXT;

-- GENERATED COLUMNS

ALTER TABLE git_repo_metric
    ADD COLUMN lines_non_blank BIGINT
        GENERATED ALWAYS AS (lines_code + lines_comment) STORED NOT NULL;

ALTER TABLE git_repo_metric
    ADD COLUMN lines BIGINT
        GENERATED ALWAYS AS (lines_code + lines_comment + lines_blank) STORED NOT NULL;

-- AGGREGATE REPOSITORY METRICS

DROP TABLE IF EXISTS git_repo_metric_aggregate;
CREATE MATERIALIZED VIEW git_repo_metric_aggregate AS
SELECT
    repo_id,
    SUM(lines_blank) AS lines_blank,
    SUM(lines_code) AS lines_code,
    SUM(lines_comment) AS lines_comment,
    SUM(lines_non_blank) AS lines_non_blank,
    SUM(lines) AS lines
FROM git_repo_metric
GROUP BY repo_id;

-- TOPIC STATISTICS

DROP TABLE IF EXISTS topic_statistics;
CREATE MATERIALIZED VIEW topic_statistics AS
SELECT
    topic.id AS topic_id,
    COUNT(repo_id) AS count
FROM topic
         LEFT JOIN git_repo_topic
                   ON topic.id = git_repo_topic.topic_id
GROUP BY topic.id;

CREATE UNIQUE INDEX topic_statistics_idx ON topic_statistics (topic_id);
CREATE INDEX topic_statistics_count_idx ON topic_statistics (count);

-- LICENSE STATISTICS

DROP TABLE IF EXISTS license_statistics;
CREATE MATERIALIZED VIEW license_statistics AS
SELECT
    license_id AS license_id,
    COUNT(id) AS count
FROM git_repo
WHERE license_id IS NOT NULL
GROUP BY license_id;

CREATE UNIQUE INDEX license_statistics_idx ON license_statistics (license_id);
CREATE INDEX license_statistics_count_idx ON license_statistics (count);

-- LABEL STATISTICS

DROP TABLE IF EXISTS label_statistics;
CREATE MATERIALIZED VIEW label_statistics AS
SELECT
    label.id AS label_id,
    COUNT(repo_id) AS count
FROM label
         LEFT JOIN git_repo_label
                   ON git_repo_label.label_id = label.id
GROUP BY label.id;

CREATE UNIQUE INDEX label_statistics_idx ON label_statistics (label_id);
CREATE INDEX label_statistics_count_idx ON label_statistics (count);

-- LANGUAGE STATISTICS

DROP TABLE IF EXISTS language_statistics;
CREATE MATERIALIZED VIEW language_statistics AS
WITH count_git_repo_by_language_mined AS (
    SELECT
        language.id AS language_id,
        COUNT(git_repo.id) AS count
    FROM language
    INNER JOIN git_repo
        ON git_repo.language_id = language.id
    GROUP BY language.id
), count_git_repo_by_language_analyzed AS (
    SELECT
        language.id AS language_id,
        COUNT(git_repo.id) AS count
    FROM language
    INNER JOIN git_repo
        ON git_repo.language_id = language.id
    INNER JOIN git_repo_metric_aggregate
        ON git_repo_metric_aggregate.repo_id = git_repo.id
    GROUP BY language.id
)
SELECT
    language.id AS language_id,
    count_git_repo_by_language_mined.count AS mined,
    count_git_repo_by_language_analyzed.count AS analyzed
FROM language
         INNER JOIN count_git_repo_by_language_mined
                    ON language.id = count_git_repo_by_language_mined.language_id
         INNER JOIN count_git_repo_by_language_analyzed
                    ON language.id = count_git_repo_by_language_analyzed.language_id;

CREATE UNIQUE INDEX language_statistics_idx ON language_statistics (language_id);
CREATE INDEX language_statistics_mined_idx ON language_statistics (mined);
CREATE INDEX language_statistics_analyzed_idx ON language_statistics (analyzed);
