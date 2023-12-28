ALTER TABLE git_repo
ADD COLUMN disabled BIT NULL
AFTER archived;

ALTER TABLE git_repo
ADD COLUMN locked BIT NULL
AFTER archived;

UPDATE git_repo
SET disabled = false
AND locked = false;
