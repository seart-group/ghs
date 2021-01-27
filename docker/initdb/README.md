How to reproduce the following .sql files from a running server?

## `0-init.sql`: 
1. No need to be updated

## `1-gse-db-schema.sql`: 

1. `docker exec gse-db mysqldump --no-tablespaces -u gseadmin -pLugano2020 --no-data gse > ./1-gse-db-schema.sql`


## `2-gse-db-data-202*-**-**.sql`

1. `docker exec gse-db mysqldump --no-tablespaces -u gseadmin -pLugano2020 --no-create-info gse > ~/gse-db-data-2021-01-14.sql`
2. Delete INSERT statements for `access_token` tables
3. Delete INSERT statements for `supported_language` tables
4. Delete INSERT statements for `flyway_schema_history` tables
   - You must also delete corresponding migration scripts, EXCEPT `V0__initialize_languages.sql` and `V1__initialize_tokens.sql`
