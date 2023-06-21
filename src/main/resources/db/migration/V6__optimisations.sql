ALTER TABLE repo_metrics MODIFY `lines_blank` BIGINT NOT NULL DEFAULT 0;
ALTER TABLE repo_metrics MODIFY `lines_code` BIGINT NOT NULL DEFAULT 0;
ALTER TABLE repo_metrics MODIFY `lines_comment` BIGINT NOT NULL DEFAULT 0;

ALTER TABLE repo_metrics ADD COLUMN `lines` BIGINT
AS (`lines_blank` + `lines_code` + `lines_comment`) VIRTUAL NOT NULL;

CREATE OR REPLACE VIEW repo_metrics_by_repo AS
SELECT
    `repo_id`,
    SUM(`lines`) AS `lines`,
    SUM(`lines_blank`) AS `lines_blank`,
    SUM(`lines_code`) AS `lines_code`,
    SUM(`lines_comment`) AS `lines_comment`
FROM `repo_metrics`
GROUP BY `repo_id`;
