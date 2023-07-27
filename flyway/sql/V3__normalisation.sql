CREATE TABLE language (
    id BIGINT NOT NULL
        PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    CONSTRAINT unique_language_name UNIQUE (name)
)
SELECT
    MAX(repo_language_id) AS id,
    repo_language_name AS name
FROM repo_language
GROUP BY repo_language_name;

CREATE TABLE _repo_language (
    repo_id BIGINT NOT NULL,
    language_id BIGINT NOT NULL,
    size_of_code BIGINT NOT NULL,
    PRIMARY KEY (repo_id, language_id)
)
SELECT
    rl.repo_id AS repo_id,
    language.id AS language_id,
    rl.size_of_code AS size_of_code
FROM (SELECT id FROM repo) r
INNER JOIN (
    SELECT
        repo_id,
        repo_language_name,
        MAX(repo_language_id) AS repo_language_id
    FROM repo_language
    GROUP BY repo_id, repo_language_name
    ORDER BY repo_id, repo_language_name
) AS normalized ON r.id = normalized.repo_id
INNER JOIN repo_language rl ON rl.repo_language_id = normalized.repo_language_id
INNER JOIN language ON rl.repo_language_name = language.name;

ALTER TABLE _repo_language ADD FOREIGN KEY (repo_id) REFERENCES repo(id);
ALTER TABLE _repo_language ADD FOREIGN KEY (language_id) REFERENCES language(id);

RENAME TABLE repo_language TO repo_language_old;
RENAME TABLE _repo_language TO repo_language;

CREATE TABLE label (
    id BIGINT NOT NULL
        PRIMARY KEY,
    name VARCHAR(256)
        CHARACTER SET utf8mb4
        COLLATE utf8mb4_unicode_ci
        NOT NULL,
    CONSTRAINT unique_label_name
        UNIQUE (name)
)
SELECT DISTINCT
    LOWER(repo_label_name) AS name,
    MAX(repo_label_id) AS id
FROM repo_label
GROUP BY name;

ALTER TABLE label MODIFY name
    VARCHAR(256)
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci
    NOT NULL
    AFTER id;

CREATE TABLE _repo_label (
    repo_id BIGINT NOT NULL,
    label_id BIGINT NOT NULL,
    PRIMARY KEY (repo_id, label_id)
)
SELECT DISTINCT
    rl.repo_id AS repo_id,
    label.id AS label_id
FROM (SELECT id FROM repo) r
INNER JOIN (
    SELECT
        repo_id,
        repo_label_name,
        MAX(repo_label_id) AS repo_label_id
    FROM repo_label
    GROUP BY repo_id, repo_label_name
    ORDER BY repo_id, repo_label_name
) AS normalized ON r.id = normalized.repo_id
INNER JOIN repo_label rl ON rl.repo_label_id = normalized.repo_label_id
INNER JOIN label ON rl.repo_label_name = label.name;

ALTER TABLE _repo_label ADD FOREIGN KEY (repo_id) REFERENCES repo(id);
ALTER TABLE _repo_label ADD FOREIGN KEY (label_id) REFERENCES label(id);

RENAME TABLE repo_label TO repo_label_old;
RENAME TABLE _repo_label TO repo_label;

ALTER TABLE topics RENAME COLUMN label TO name;
RENAME TABLE topics TO topic;
RENAME TABLE repo_topics TO repo_topic;

DROP TABLE repo_language_old;
DROP TABLE repo_label_old;
