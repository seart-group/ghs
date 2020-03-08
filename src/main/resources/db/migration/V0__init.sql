-- To create DB manually in SQL console use:
-- CREATE DATABASE gse CHARACTER SET utf8 COLLATE utf8_bin;

create table repo
(
    id bigint not null
        primary key,
    name varchar(255) null,
    is_fork_project bit null,
    commits bigint null,
    branches bigint null,
    default_branch varchar(255) null,
    releases bigint null,
    contributors bigint null,
    license varchar(255) null,
    watchers bigint null,
    stargazers bigint null,
    forks bigint null,
    size bigint null,
    main_language varchar(255) null,
    total_issues bigint null,
    open_issues bigint null,
    total_pull_requests bigint null,
    opened_pull_requests bigint null,
    last_commit datetime null,
    last_commit_sha varchar(255) null,
    has_wiki bit null,
    archived bit null,
    constraint unique_repo_name
        unique (name)
);

create table repo_label
(
    repo_label_id bigint not null,
    repo_id bigint not null,
    repo_label_name varchar(255) null,
    primary key (repo_label_id, repo_id)
);

create index repo_labels_index_repo_id
    on repo_labels (repo_id);

create table repo_language
(
    repo_language_id bigint not null,
    repo_id bigint not null,
    repo_language_name varchar(255) null,
    size_of_code bigint not null,
    primary key (repo_language_id, repo_id)
);

create index repo_languages_index_repo_id
    on repo_languages (repo_id);

CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

insert into hibernate_sequence (next_val) values (0);