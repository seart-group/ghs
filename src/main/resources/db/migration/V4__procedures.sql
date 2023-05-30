CREATE PROCEDURE signal_table_immutable()
BEGIN
    SIGNAL SQLSTATE 'GSE00' SET
    MESSAGE_TEXT = 'Table modification not allowed!',
    MYSQL_ERRNO = 1000;
END;
