-- ${flyway:timestamp}

DROP TABLE IF EXISTS topic_ranked;
CREATE TABLE IF NOT EXISTS topic_ranked (
    name VARCHAR(256) NOT NULL
)
SELECT t.name AS name
FROM topic t
INNER JOIN git_repo_topic rt ON t.id = rt.topic_id
GROUP BY t.id
ORDER BY COUNT(rt.repo_id) DESC;

CREATE TRIGGER topic_ranked_insert
BEFORE INSERT ON topic_ranked
FOR EACH ROW
BEGIN
	CALL signal_table_immutable();
END;

CREATE TRIGGER topic_ranked_update
BEFORE UPDATE ON topic_ranked
FOR EACH ROW
BEGIN
	CALL signal_table_immutable();
END;

CREATE TRIGGER topic_ranked_delete
BEFORE DELETE ON topic_ranked
FOR EACH ROW
BEGIN
	CALL signal_table_immutable();
END;
