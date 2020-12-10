UPDATE access_token SET value = 'fa7503e89159496c277845ed7771f2d9ca302081', added='2020-12-08 15:00:00' WHERE id=1;
CREATE INDEX id_index ON repo (id);
CREATE INDEX search_col_index ON repo (name, main_language, license, commits, contributors, total_issues, open_issues, total_pull_requests, open_pull_requests, branches, releases, stargazers, watchers, forks, created_at, pushed_at);
CREATE INDEX main_lang_index ON repo (main_language);
CREATE INDEX bool_col_index ON repo(has_wiki, is_fork_project);
