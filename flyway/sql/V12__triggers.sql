DROP EVENT language_statistics_refresh;
DROP PROCEDURE language_statistics_update;

DROP TABLE IF EXISTS language_statistics;
CREATE TABLE language_statistics (
    language_id BIGINT NOT NULL
        PRIMARY KEY,
    mined BIGINT NOT NULL,
    analyzed BIGINT NOT NULL
)
SELECT language_id, mined, analyzed
FROM count_git_repo_by_language;

CREATE TRIGGER git_repo_insert
AFTER INSERT ON git_repo
FOR EACH ROW
BEGIN
    DECLARE _language_id BIGINT;
    SET _language_id = NEW.language_id;
    UPDATE language_statistics
    SET mined = mined + 1
    WHERE language_id = _language_id;
END;

CREATE TRIGGER git_repo_delete
AFTER DELETE ON git_repo
FOR EACH ROW
BEGIN
    DECLARE _language_id BIGINT;
    SET _language_id = OLD.language_id;
    UPDATE language_statistics
    SET mined = mined - 1
    WHERE language_id = _language_id;
END;

CREATE TRIGGER git_repo_metric_aggregate_insert
AFTER INSERT ON git_repo_metric_aggregate
FOR EACH ROW
BEGIN
    DECLARE _language_id BIGINT;
    SELECT language_id INTO _language_id
    FROM git_repo WHERE id = NEW.repo_id;
    UPDATE language_statistics
    SET analyzed = analyzed + 1
    WHERE language_id = _language_id;
END;

CREATE TRIGGER git_repo_metric_aggregate_delete
AFTER DELETE ON git_repo_metric_aggregate
FOR EACH ROW
BEGIN
    DECLARE _language_id BIGINT;
    SELECT language_id INTO _language_id
    FROM git_repo WHERE id = OLD.repo_id;
    UPDATE language_statistics
    SET analyzed = analyzed - 1
    WHERE language_id = _language_id;
END;
