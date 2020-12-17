-- Freeing empty space after C and CPP (5:Python, 6:JavaScript, 7:Typescript)
UPDATE supported_language SET ID=ID+1 WHERE ID>=5 ORDER BY ID DESC;
INSERT INTO supported_language VALUES (5, 'CSharp', '2020-12-17 10:30:00');
