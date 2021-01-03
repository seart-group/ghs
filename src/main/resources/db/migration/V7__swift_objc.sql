-- delete Ozren's token
DELETE FROM access_token WHERE value='faa5d7ae42793a29c360572fd519d5438d41962b';
-- line below reset AUTO_INCREMENT value of id column back to MAX+1
ALTER TABLE supported_language AUTO_INCREMENT = 0;
INSERT INTO supported_language (name,added) VALUES ('Swift', current_timestamp);
INSERT INTO supported_language (name,added) VALUES ('Objective-C', current_timestamp);
