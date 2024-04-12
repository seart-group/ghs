DROP TABLE IF EXISTS topic_ranked;

CREATE OR REPLACE VIEW count_git_repo_by_topic AS
SELECT
    topic.id AS topic_id,
    COUNT(repo_id) AS count
FROM topic
LEFT JOIN git_repo_topic
    ON topic.id = git_repo_topic.topic_id
GROUP BY topic.id;

CREATE TABLE topic_statistics (
    topic_id BIGINT NOT NULL
        PRIMARY KEY,
    count BIGINT NOT NULL
)
SELECT * FROM count_git_repo_by_topic;

CREATE INDEX count_idx ON topic_statistics (count);

CREATE TRIGGER topic_insert
AFTER INSERT ON topic
FOR EACH ROW
BEGIN
    INSERT INTO topic_statistics (topic_id, count)
    VALUES (NEW.id, 0);
END;

CREATE TRIGGER topic_delete
AFTER DELETE ON topic
FOR EACH ROW
BEGIN
    DELETE FROM topic_statistics
    WHERE topic_id = OLD.id;
END;

CREATE TRIGGER git_repo_topic_insert
AFTER INSERT ON git_repo_topic
FOR EACH ROW
BEGIN
    UPDATE topic_statistics
    SET count = count + 1
    WHERE topic_id = NEW.topic_id;
END;

CREATE TRIGGER git_repo_topic_delete
AFTER DELETE ON git_repo_topic
FOR EACH ROW
BEGIN
    UPDATE topic_statistics
    SET count = count - 1
    WHERE topic_id = OLD.topic_id;
END;

CREATE TRIGGER git_repo_topic_update
AFTER UPDATE ON git_repo_topic
FOR EACH ROW
BEGIN
    UPDATE topic_statistics
    SET count = count + 1
    WHERE topic_id = NEW.topic_id;
    UPDATE topic_statistics
    SET count = count - 1
    WHERE topic_id = OLD.topic_id;
END;
