ALTER TABLE git_repo ADD COLUMN language_id BIGINT DEFAULT NULL AFTER id;

UPDATE git_repo
INNER JOIN language
    ON language.name = git_repo.main_language
SET git_repo.language_id = language.id;

ALTER TABLE git_repo MODIFY language_id BIGINT NOT NULL;
ALTER TABLE git_repo ADD CONSTRAINT FOREIGN KEY (language_id) REFERENCES language(id);
ALTER TABLE git_repo DROP COLUMN main_language;
