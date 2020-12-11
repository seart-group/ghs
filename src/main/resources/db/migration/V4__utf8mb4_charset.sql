ALTER TABLE repo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE repo_label DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE repo MODIFY COLUMN name varchar(140) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE repo MODIFY COLUMN default_branch varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE repo MODIFY COLUMN homepage varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE repo_label MODIFY COLUMN repo_label_name varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
