CREATE PROCEDURE get_and_increment_hibernate_sequence_value(OUT next BIGINT)
BEGIN
    START TRANSACTION;
    SELECT next_val INTO next
    FROM hibernate_sequence LIMIT 1;
    UPDATE hibernate_sequence
    SET next_val = next + 1 LIMIT 1;
    COMMIT;
    SELECT @next;
END;

CREATE PROCEDURE populate_license_table()
BEGIN
    DECLARE _id BIGINT;
    DECLARE _name VARCHAR(256);
    DECLARE _is_done BIT DEFAULT FALSE;
    DECLARE _cursor CURSOR FOR
        SELECT DISTINCT name
        FROM license_view;
    DECLARE CONTINUE HANDLER
    FOR NOT FOUND SET _is_done = TRUE;

    OPEN _cursor;
    read_loop: LOOP
        FETCH _cursor INTO _name;
        IF _is_done THEN LEAVE read_loop;
        END IF;
        CALL get_and_increment_hibernate_sequence_value(_id);
        INSERT INTO license VALUES (_id, _name);
        UPDATE git_repo SET license_id = _id WHERE license = _name;
    END LOOP;
    CLOSE _cursor;
END;

RENAME TABLE license TO license_view;
CREATE TABLE license (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(256)
        CHARACTER SET utf8mb4
        COLLATE utf8mb4_unicode_ci
        NOT NULL,
    CONSTRAINT unique_license_name
        UNIQUE (name)
);

ALTER TABLE git_repo
ADD COLUMN license_id BIGINT DEFAULT NULL
AFTER language_id;

CALL populate_license_table();

ALTER TABLE git_repo
ADD FOREIGN KEY (license_id) REFERENCES license(id);

ALTER TABLE git_repo
DROP COLUMN license;
DROP VIEW license_view;

DROP PROCEDURE get_and_increment_hibernate_sequence_value;
DROP PROCEDURE populate_license_table;
