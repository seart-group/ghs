CREATE OR REPLACE VIEW count_label_by_git_repo AS
SELECT
    label_id,
    COUNT(repo_id) AS count
FROM git_repo_label
GROUP BY label_id;

CREATE TABLE label_statistics (
    label_id BIGINT NOT NULL
        PRIMARY KEY,
    count BIGINT NOT NULL
)
SELECT * FROM count_label_by_git_repo;

CREATE INDEX count_idx ON label_statistics (count);

CREATE TRIGGER label_insert
AFTER INSERT ON label
FOR EACH ROW
BEGIN
    INSERT INTO label_statistics (label_id, count)
    VALUES (NEW.id, 0);
END;

CREATE TRIGGER label_delete
AFTER DELETE ON label
FOR EACH ROW
BEGIN
    DELETE FROM label_statistics
    WHERE label_id = OLD.id;
END;

CREATE TRIGGER git_repo_label_insert
AFTER INSERT ON git_repo_label
FOR EACH ROW
BEGIN
    UPDATE label_statistics
    SET count = count + 1
    WHERE label_id = NEW.label_id;
END;

CREATE TRIGGER git_repo_label_delete
AFTER DELETE ON git_repo_label
FOR EACH ROW
BEGIN
    UPDATE label_statistics
    SET count = count - 1
    WHERE label_id = OLD.label_id;
END;

CREATE TRIGGER git_repo_label_update
AFTER UPDATE ON git_repo_label
FOR EACH ROW
BEGIN
    UPDATE label_statistics
    SET count = count + 1
    WHERE label_id = NEW.label_id;
    UPDATE label_statistics
    SET count = count - 1
    WHERE label_id = OLD.label_id;
END;
