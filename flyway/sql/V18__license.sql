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
