RENAME TABLE repo TO git_repo;
RENAME TABLE repo_label TO git_repo_label;
RENAME TABLE repo_language TO git_repo_language;
RENAME TABLE repo_metrics TO git_repo_metrics;
RENAME TABLE repo_topic TO git_repo_topic;

CREATE OR REPLACE VIEW license AS
SELECT DISTINCT r.license AS name
FROM git_repo r
WHERE r.license IS NOT NULL
ORDER BY name;

DROP VIEW count_repo_by_main_language;
CREATE OR REPLACE VIEW count_git_repo_by_main_language AS
SELECT
    main_language AS name,
    COUNT(id) AS count
FROM git_repo
GROUP BY main_language;

DROP VIEW repo_metrics_by_repo;
CREATE OR REPLACE VIEW git_repo_metrics_by_id AS
SELECT
    `repo_id`,
    CAST(SUM(`lines_blank`) AS SIGNED INTEGER) AS `lines_blank`,
    CAST(SUM(`lines_code`) AS SIGNED INTEGER) AS `lines_code`,
    CAST(SUM(`lines_comment`) AS SIGNED INTEGER) AS `lines_comment`,
    CAST(SUM(`lines`) AS SIGNED INTEGER) AS `lines`,
    CAST(SUM(`lines_non_blank`) AS SIGNED INTEGER) AS `lines_non_blank`
FROM `git_repo_metrics`
GROUP BY `repo_id`;
