-- ${flyway:timestamp}

UPDATE license_statistics
INNER JOIN count_git_repo_by_license
    ON license_statistics.license_id = count_git_repo_by_license.license_id
SET license_statistics.count = count_git_repo_by_license.count
WHERE license_statistics.count != count_git_repo_by_license.count;
