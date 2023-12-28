ALTER TABLE git_repo
ADD COLUMN disabled BIT NULL
AFTER archived;

UPDATE git_repo SET disabled = false;
