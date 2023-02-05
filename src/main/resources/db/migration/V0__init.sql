-- To CREATE DB manually in MySQL console use:
-- CREATE DATABASE gse CHARACTER SET utf8 COLLATE utf8_bin;
-- To ensure that the time zone is set to UTC
-- SET GLOBAL time_zone = '+0:00';
-- SELECT @@global.time_zone, @@session.time_zone;

CREATE TABLE repo
(
    id BIGINT NOT NULL
        PRIMARY KEY,
    name VARCHAR(140) NULL,
    is_fork_project BIT NULL,
    commits BIGINT NULL,
    branches BIGINT NULL,
    default_branch VARCHAR(256) NULL,
    releases BIGINT NULL,
    contributors BIGINT NULL,
    license VARCHAR(64) NULL,
    watchers BIGINT NULL,
    stargazers BIGINT NULL,
    forks BIGINT NULL,
    size BIGINT NULL,
    CREATEd_at TIMESTAMP NULL,
    pushed_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    homepage VARCHAR(2048) NULL,
    main_language VARCHAR(64) NULL,
    total_issues BIGINT NULL,
    open_issues BIGINT NULL,
    total_pull_requests BIGINT NULL,
    open_pull_requests BIGINT NULL,
    last_commit TIMESTAMP NULL,
    last_commit_sha VARCHAR(40) NULL,
    has_wiki BIT NULL,
    archived BIT NULL,
    crawled TIMESTAMP NULL,
    CONSTRAINT unique_repo_name
        UNIQUE (name)
);

CREATE TABLE repo_label
(
    repo_label_id BIGINT NOT NULL,
    repo_id BIGINT NOT NULL,
    repo_label_name VARCHAR(60) NULL,
    crawled TIMESTAMP NULL,
    PRIMARY KEY (repo_label_id, repo_id)
);

CREATE INDEX repo_label_index_repo_id
    ON repo_label (repo_id);

CREATE TABLE repo_language
(
    repo_language_id BIGINT NOT NULL,
    repo_id BIGINT NOT NULL,
    repo_language_name VARCHAR(64) NULL,
    size_of_code BIGINT NOT NULL,
    crawled TIMESTAMP NULL,
    PRIMARY KEY (repo_language_id, repo_id)
);

CREATE INDEX repo_language_index_repo_id
    ON repo_language (repo_id);

CREATE TABLE access_token
(
    id BIGINT auto_increment
        PRIMARY KEY,
    value VARCHAR(40) NULL,
    added TIMESTAMP NULL,
    CONSTRAINT unique_token
        UNIQUE (value)
);

CREATE TABLE supported_language
(
    id BIGINT auto_increment
        PRIMARY KEY,
    name VARCHAR(64) NULL,
    added TIMESTAMP NULL,
    CONSTRAINT unique_language
        UNIQUE (name)
);

CREATE TABLE crawl_job
(
    crawl_id BIGINT NOT NULL,
    crawled DATETIME NULL,
    language_id BIGINT NOT NULL,
    PRIMARY KEY (crawl_id, language_id)
);

CREATE INDEX crawl_job_index_language_id
    ON crawl_job (language_id);

CREATE TABLE `hibernate_sequence` (
  `next_val` BIGINT(20) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

INSERT INTO hibernate_sequence (next_val) VALUES (0);
