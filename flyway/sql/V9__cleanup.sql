ALTER TABLE git_repo RENAME COLUMN crawled TO last_pinged;
ALTER TABLE git_repo RENAME COLUMN cloned TO last_analyzed;
