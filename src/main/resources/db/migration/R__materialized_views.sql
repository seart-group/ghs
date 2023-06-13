-- ${flyway:timestamp}

DROP TABLE IF EXISTS label_ranked;
CREATE TABLE IF NOT EXISTS label_ranked (
    name VARCHAR(256)
        CHARACTER SET utf8mb4
        COLLATE utf8mb4_unicode_ci
        NOT NULL
)
SELECT l.name AS name
FROM label l
INNER JOIN repo_label rl ON l.id = rl.label_id
GROUP BY l.id
ORDER BY COUNT(rl.repo_id) DESC;

CREATE TRIGGER label_ranked_no_insert
BEFORE INSERT ON label_ranked
FOR EACH ROW
BEGIN
	CALL signal_table_immutable();
END;

CREATE TRIGGER label_ranked_no_update
BEFORE UPDATE ON label_ranked
FOR EACH ROW
BEGIN
	CALL signal_table_immutable();
END;

CREATE TRIGGER label_ranked_no_delete
BEFORE DELETE ON label_ranked
FOR EACH ROW
BEGIN
	CALL signal_table_immutable();
END;

DROP TABLE IF EXISTS topic_ranked;
CREATE TABLE IF NOT EXISTS topic_ranked (
    name VARCHAR(256) NOT NULL
)
SELECT t.name AS name
FROM topic t
INNER JOIN repo_topic rt ON t.id = rt.topic_id
GROUP BY t.id
ORDER BY COUNT(rt.repo_id) DESC;

CREATE TRIGGER topic_ranked_no_insert
BEFORE INSERT ON topic_ranked
FOR EACH ROW
BEGIN
	CALL signal_table_immutable();
END;

CREATE TRIGGER topic_ranked_no_update
BEFORE UPDATE ON topic_ranked
FOR EACH ROW
BEGIN
	CALL signal_table_immutable();
END;

CREATE TRIGGER topic_ranked_no_delete
BEFORE DELETE ON topic_ranked
FOR EACH ROW
BEGIN
	CALL signal_table_immutable();
END;
