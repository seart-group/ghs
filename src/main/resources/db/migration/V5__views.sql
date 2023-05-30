CREATE INDEX license_idx ON repo(license);

CREATE OR REPLACE VIEW license AS
SELECT DISTINCT r.license AS name
FROM repo r
WHERE r.license IS NOT NULL
ORDER BY name;

CREATE OR REPLACE VIEW count_repo_by_main_language AS
SELECT
    main_language AS name,
    COUNT(id) AS count
FROM repo
GROUP BY main_language
ORDER BY count DESC;
