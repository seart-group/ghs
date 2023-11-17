CREATE TABLE git_repo_metric_aggregate (
    `repo_id` BIGINT NOT NULL
        PRIMARY KEY,
    `lines_blank` BIGINT NOT NULL,
    `lines_code` BIGINT NOT NULL,
    `lines_comment` BIGINT NOT NULL,
    `lines` BIGINT NOT NULL,
    `lines_non_blank` BIGINT NOT NULL
) SELECT * FROM git_repo_metrics_by_id;

ALTER TABLE git_repo_metric_aggregate
ADD CONSTRAINT FOREIGN KEY (`repo_id`) REFERENCES git_repo(`id`);

CREATE INDEX git_repo_metric_aggregate_idx ON git_repo_metric_aggregate(`lines_non_blank`, `lines_code`, `lines_comment`);

RENAME TABLE git_repo_metrics TO git_repo_metric;

CREATE TRIGGER git_repo_metric_insert
AFTER INSERT ON git_repo_metric
FOR EACH ROW
BEGIN
    DECLARE `_repo_id` BIGINT;

    DECLARE `lines_blank_delta` INTEGER;
    DECLARE `lines_code_delta` INTEGER;
    DECLARE `lines_comment_delta` INTEGER;
    DECLARE `lines_delta` INTEGER;
    DECLARE `lines_non_blank_delta` INTEGER;

    DECLARE initialized BIT;

    SET `_repo_id` = NEW.`repo_id`;

    SET `lines_blank_delta` = NEW.`lines_blank`;
    SET `lines_code_delta` = NEW.`lines_code`;
    SET `lines_comment_delta` = NEW.`lines_comment`;
    SET `lines_delta` = NEW.`lines`;
    SET `lines_non_blank_delta` = NEW.`lines_non_blank`;

    SELECT EXISTS(
        SELECT `repo_id`
        FROM git_repo_metric_aggregate
        WHERE `repo_id` = `_repo_id`
    ) INTO initialized;

    IF initialized THEN
        UPDATE git_repo_metric_aggregate
        SET
            `lines_blank` = `lines_blank` + `lines_blank_delta`,
            `lines_code` = `lines_code` + `lines_code_delta`,
            `lines_comment` = `lines_comment` + `lines_comment_delta`,
            `lines` = `lines` + `lines_delta`,
            `lines_non_blank` = `lines_non_blank` + `lines_non_blank_delta`
        WHERE `repo_id` = `_repo_id`;
    ELSE
        INSERT INTO git_repo_metric_aggregate VALUE (
            `_repo_id`,
            `lines_blank_delta`,
            `lines_code_delta`,
            `lines_comment_delta`,
            `lines_delta`,
            `lines_non_blank_delta`
        );
    END IF;
END;

CREATE TRIGGER git_repo_metric_delete
AFTER DELETE ON git_repo_metric
FOR EACH ROW
BEGIN
    DECLARE `_repo_id` BIGINT;

    DECLARE `lines_blank_delta` INTEGER;
    DECLARE `lines_code_delta` INTEGER;
    DECLARE `lines_comment_delta` INTEGER;
    DECLARE `lines_delta` INTEGER;
    DECLARE `lines_non_blank_delta` INTEGER;

    DECLARE `lines_blank_current` INTEGER;
    DECLARE `lines_code_current` INTEGER;
    DECLARE `lines_comment_current` INTEGER;
    DECLARE `lines_current` INTEGER;
    DECLARE `lines_non_blank_current` INTEGER;

    SET  `_repo_id` = OLD.`repo_id`;

    SET `lines_blank_delta` = -OLD.`lines_blank`;
    SET `lines_code_delta` = -OLD.`lines_code`;
    SET `lines_comment_delta` = -OLD.`lines_comment`;
    SET `lines_delta` = -OLD.`lines`;
    SET `lines_non_blank_delta` = -OLD.`lines_non_blank`;

    SELECT
        `lines_blank` + `lines_blank_delta` AS lines_blank_current,
        `lines_code` + `lines_code_delta` AS lines_code_current,
        `lines_comment` + `lines_comment_delta` AS lines_comment_current,
        `lines` + `lines_delta` AS lines_current,
        `lines_non_blank` + `lines_non_blank_delta` AS lines_non_blank_current
    INTO
        `lines_blank_current`,
        `lines_code_current`,
        `lines_comment_current`,
        `lines_current`,
        `lines_non_blank_current`
    FROM git_repo_metric_aggregate
    WHERE `repo_id` = `_repo_id`;

    IF (
        `lines_blank_current` = 0 AND
        `lines_code_current` = 0 AND
        `lines_comment_current` = 0 AND
        `lines_current` = 0 AND
        `lines_non_blank_current` = 0
    ) THEN
        DELETE FROM git_repo_metric_aggregate
        WHERE `repo_id` = `_repo_id`;
    ELSE
        UPDATE git_repo_metric_aggregate
        SET
            `lines_blank` = `lines_blank_current`,
            `lines_code` = `lines_code_current`,
            `lines_comment` = `lines_comment_current`,
            `lines` = `lines_current`,
            `lines_non_blank` = `lines_non_blank_current`
        WHERE `repo_id` = `_repo_id`;
    END IF;
END;

CREATE TRIGGER git_repo_metric_update
AFTER UPDATE ON git_repo_metric
FOR EACH ROW
BEGIN
    DECLARE `_repo_id` BIGINT;

    DECLARE `lines_blank_delta` INTEGER;
    DECLARE `lines_code_delta` INTEGER;
    DECLARE `lines_comment_delta` INTEGER;
    DECLARE `lines_delta` INTEGER;
    DECLARE `lines_non_blank_delta` INTEGER;

    DECLARE `lines_blank_current` INTEGER;
    DECLARE `lines_code_current` INTEGER;
    DECLARE `lines_comment_current` INTEGER;
    DECLARE `lines_current` INTEGER;
    DECLARE `lines_non_blank_current` INTEGER;

    SET  `_repo_id` = NEW.`repo_id`;

    SET `lines_blank_delta` = NEW.`lines_blank` - OLD.`lines_blank`;
    SET `lines_code_delta` = NEW.`lines_code` - OLD.`lines_code`;
    SET `lines_comment_delta` = NEW.`lines_comment` - OLD.`lines_comment`;
    SET `lines_delta` = NEW.`lines` - OLD.`lines`;
    SET `lines_non_blank_delta` = NEW.`lines_non_blank` - OLD.`lines_non_blank`;

    SELECT
        `lines_blank` + `lines_blank_delta` AS `lines_blank_current`,
        `lines_code` + `lines_code_delta` AS `lines_code_current`,
        `lines_comment` + `lines_comment_delta` AS `lines_comment_current`,
        `lines` + `lines_delta` AS `lines_current`,
        `lines_non_blank` + `lines_non_blank_delta` AS `lines_non_blank_current`
    INTO
        `lines_blank_current`,
        `lines_code_current`,
        `lines_comment_current`,
        `lines_current`,
        `lines_non_blank_current`
    FROM git_repo_metric_aggregate
    WHERE `repo_id` = `_repo_id`;

    IF (
        `lines_blank_current` = 0 AND
        `lines_code_current` = 0 AND
        `lines_comment_current` = 0 AND
        `lines_current` = 0 AND
        `lines_non_blank_current` = 0
    ) THEN
        DELETE FROM git_repo_metric_aggregate
        WHERE `repo_id` = `_repo_id`;
    ELSE
        UPDATE git_repo_metric_aggregate
        SET
            `lines_blank` = `lines_blank_current`,
            `lines_code` = `lines_code_current`,
            `lines_comment` = `lines_comment_current`,
            `lines` = `lines_current`,
            `lines_non_blank` = `lines_non_blank_current`
        WHERE `repo_id` = `_repo_id`;
    END IF;
END;

DROP VIEW git_repo_metrics_by_id;
