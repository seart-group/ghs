-- To create DB manually in SQL console use:
-- CREATE DATABASE gse CHARACTER SET utf8 COLLATE utf8_bin;

create table repo
(
    id bigint not null
        primary key,
    name varchar(140) null,
    is_fork_project bit null,
    commits bigint null,
    branches bigint null,
    default_branch varchar(256) null,
    releases bigint null,
    contributors bigint null,
    license varchar(64) null,
    watchers bigint null,
    stargazers bigint null,
    forks bigint null,
    size bigint null,
    created_at timestamp null,
    pushed_at timestamp null,
    updated_at timestamp null,
    homepage varchar(2048) null,
    main_language varchar(64) null,
    total_issues bigint null,
    open_issues bigint null,
    total_pull_requests bigint null,
    open_pull_requests bigint null,
    last_commit timestamp null,
    last_commit_sha varchar(40) null,
    has_wiki bit null,
    archived bit null,
    crawled timestamp null,
    constraint unique_repo_name
        unique (name)
);

create table repo_label
(
    repo_label_id bigint not null,
    repo_id bigint not null,
    repo_label_name varchar(60) null,
    crawled timestamp null,
    primary key (repo_label_id, repo_id)
);

create index repo_label_index_repo_id
    on repo_label (repo_id);

create table repo_language
(
    repo_language_id bigint not null,
    repo_id bigint not null,
    repo_language_name varchar(64) null,
    size_of_code bigint not null,
    crawled timestamp null,
    primary key (repo_language_id, repo_id)
);

create index repo_language_index_repo_id
    on repo_language (repo_id);

create table access_token
(
    id bigint auto_increment
        primary key,
    value varchar(40) null,
    added timestamp null,
    constraint unique_token
        unique (value)
);

create table supported_language
(
    id bigint auto_increment
        primary key,
    name varchar(64) null,
    added timestamp null,
    constraint unique_language
        unique (name)
);

create table crawl_job
(
    crawl_id bigint not null,
    crawled datetime null,
    language_id bigint not null,
    primary key (crawl_id, language_id)
);

create index crawl_job_index_language_id
    on crawl_job (language_id);

CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

insert into hibernate_sequence (next_val) values (0);