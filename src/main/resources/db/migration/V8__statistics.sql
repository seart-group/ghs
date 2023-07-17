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
