INSERT INTO language (id, name)
SELECT id, language AS name
FROM metric_language
WHERE NOT EXISTS(
    SELECT name FROM language WHERE name = metric_language.language
);

ALTER TABLE git_repo ADD COLUMN language_id BIGINT DEFAULT NULL AFTER id;

UPDATE git_repo
INNER JOIN language
    ON language.name = git_repo.main_language
SET git_repo.language_id = language.id;

ALTER TABLE git_repo MODIFY language_id BIGINT NOT NULL;
ALTER TABLE git_repo ADD CONSTRAINT FOREIGN KEY (language_id) REFERENCES language(id);
ALTER TABLE git_repo DROP COLUMN main_language;

DROP VIEW count_git_repo_by_main_language;

CREATE OR REPLACE VIEW count_git_repo_by_language_mined AS
SELECT
    language.id AS language_id,
    COUNT(git_repo.id) AS count
FROM language
INNER JOIN git_repo
    ON git_repo.language_id = language.id
GROUP BY language.id;

CREATE OR REPLACE VIEW count_git_repo_by_language_analyzed AS
SELECT
    language.id AS language_id,
    COUNT(git_repo.id) AS count
FROM language
INNER JOIN git_repo
    ON git_repo.language_id = language.id
INNER JOIN git_repo_metrics_by_id
    ON git_repo_metrics_by_id.repo_id = git_repo.id
GROUP BY language.id;

CREATE OR REPLACE VIEW count_git_repo_by_language AS
SELECT
    mined.language_id AS language_id,
    mined.count AS mined,
    analyzed.count AS analyzed
FROM count_git_repo_by_language_mined mined
INNER JOIN count_git_repo_by_language_analyzed analyzed
    ON mined.language_id = analyzed.language_id;

CREATE TABLE language_statistics (
    language_id BIGINT NOT NULL
        PRIMARY KEY,
    mined BIGINT NOT NULL,
    analyzed BIGINT NOT NULL
)
SELECT * FROM count_git_repo_by_language;

ALTER TABLE language_statistics ADD CHECK (mined >= analyzed);
ALTER TABLE language_statistics ADD FOREIGN KEY (language_id) REFERENCES language(id);

ALTER TABLE git_repo_metrics ADD COLUMN language_id BIGINT DEFAULT NULL AFTER repo_id;
UPDATE git_repo_metrics
INNER JOIN metric_language
    ON git_repo_metrics.metric_language_id = metric_language.id
INNER JOIN language
    ON language.name = metric_language.language
SET git_repo_metrics.language_id = language.id;
ALTER TABLE git_repo_metrics MODIFY language_id BIGINT NOT NULL;
ALTER TABLE git_repo_metrics DROP PRIMARY KEY;
ALTER TABLE git_repo_metrics DROP FOREIGN KEY git_repo_metrics_ibfk_1;
ALTER TABLE git_repo_metrics DROP FOREIGN KEY git_repo_metrics_ibfk_2;
ALTER TABLE git_repo_metrics DROP CONSTRAINT unique_composite_key;
ALTER TABLE git_repo_metrics ADD PRIMARY KEY (repo_id, language_id);
ALTER TABLE git_repo_metrics ADD CONSTRAINT FOREIGN KEY (repo_id) REFERENCES git_repo(id);
ALTER TABLE git_repo_metrics ADD CONSTRAINT FOREIGN KEY (language_id) REFERENCES language(id);
ALTER TABLE git_repo_metrics DROP COLUMN metric_language_id;

DROP TABLE metric_language;

CREATE PROCEDURE language_statistics_update()
BEGIN
    UPDATE language_statistics AS target
        INNER JOIN count_git_repo_by_language AS source
        ON source.language_id = target.language_id
    SET
        target.mined = source.mined,
        target.analyzed = source.analyzed;
END;

CREATE EVENT language_statistics_refresh
ON SCHEDULE
    EVERY 5 MINUTE
    STARTS CURRENT_TIMESTAMP
               + INTERVAL (5 - MINUTE(CURRENT_TIMESTAMP) % 5) MINUTE
               - INTERVAL SECOND(CURRENT_TIMESTAMP) SECOND
ENABLE
DO CALL language_statistics_update();
