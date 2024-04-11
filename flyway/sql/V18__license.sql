CREATE OR REPLACE VIEW count_git_repo_by_license AS
SELECT
    license_id AS license_id,
    COUNT(id) AS count
FROM git_repo
WHERE license_id IS NOT NULL
GROUP BY license_id
ORDER BY count DESC;

CREATE TABLE license_statistics (
    license_id BIGINT NOT NULL
        PRIMARY KEY,
    count BIGINT NOT NULL
)
SELECT * FROM count_git_repo_by_license;

CREATE INDEX count_idx ON license_statistics (count);

CREATE PROCEDURE license_statistics_refresh()
BEGIN
    DECLARE _start TIMESTAMP;
    DECLARE _end TIMESTAMP;

    SET _start = NOW();

    START TRANSACTION;
    REPLACE INTO license_statistics
    SELECT * FROM count_git_repo_by_license;
    COMMIT;

    SET _end = NOW();

    INSERT INTO event_log (name, start, end)
    VALUE ('license_statistics_refresh', _start, _end);
END;

CREATE EVENT license_statistics_refresh
ON SCHEDULE
    EVERY 1 HOUR
    STARTS CURRENT_TIMESTAMP
                + INTERVAL 1 HOUR
                - INTERVAL MINUTE(CURRENT_TIMESTAMP) MINUTE
                - INTERVAL SECOND(CURRENT_TIMESTAMP) SECOND
                - INTERVAL MICROSECOND(CURRENT_TIMESTAMP) MICROSECOND
DO CALL license_statistics_refresh();
