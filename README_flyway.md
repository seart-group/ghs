
<!-- 
	Author: Emad Aghajani 
	Note:	This file is based on my experience and by no means can be considered as an official instruction.
-->


# Essential Knowledge Related to Flyway

## How to update database
Since the db is under track by Flyway in this project, for any modification (both in schema and data), you have to follow these instructions:
1. Make sure to add `logging.level.org.flywaydb=debug` in `application.properties` to get a verbose migration progress report. (**highly recommended**)
2. Create a new SQL file named `db.migration/V[next_number]__[your_description].sql` file
3. Withing the file write your SQL commands (`DELETE FROM ...`, `INSERT INTO ...`, `ALTER TABLE ...`, ...)
	- **Don't forget tailing `;` after each line**.
	- You can use `-- comment` syntax, but **you can not use it inline**!
	- Optionally you can use `current_timestamp` to refer to current time for columns with `TIMESTAMP` datatype
4. Run the app normally. Flyway automatically apply you migrations. Here's how:
	- Flyway keep history of previous migrations in its own table: `flyway_schema_history`
	- It always compares this history list, with files in `db.migration` folder
	- If it finds a new file:
		- It runs that files
		- Add a new row in `flyway_schema_history` (even in case of failure!)

## What should I do if migration fails
If migrations fails, app will not run, and you will be stuck, unless you handle the migration failure:
1. First delete the new row from `flyway_schema_history` manually (it indicates an unsuccessful migration)
2. Manually undo any db changes (I think this is not needed, as Flyway rollback in case of failure, but worth checking anyway)
3. Fix your migration files errors.
4. Run again.


## Remember (Important!)
- Always test migration on a development environment. Things can easily go wrong!
- Make sure flyway logging is enabled (Add `logging.level.org.flywaydb=debug` to `application.properties`)
- Never touch existing `db.migration/Vx__xx.sql` files. Otherwise, Flyway validation fails and gives error below, and prevent the app to run.
  > FlywayException: Validate failed: **Migration checksum mismatch** for migration version X




##### Disclaimer
This file is based on Emad's experience and by no means can be considered as an official instruction.
