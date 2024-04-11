CREATE OR REPLACE VIEW license_statistics AS
SELECT
    license_id AS license_id,
    COUNT(id) AS count
FROM git_repo
WHERE license_id IS NOT NULL
GROUP BY license_id
ORDER BY count DESC;
