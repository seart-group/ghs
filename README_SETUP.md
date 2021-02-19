# Installation and Usage

**Table of Content**:
1. [Setup MySQL](#1-setup-mysql)
2. [Setup Crawler](#2-setup-crawler)
3. [Running the `application`](#3-running-the-application)
   1. Running in IntelliJ
   2. Running in the terminal
   3. Running using `.jar`
   4. [Supported arguments](#supported-arguments)
4. [Running the `frontend`](#4-running-the-frontend)

---

## 1. Setup MySQL

<details>
<summary>
For the project to work, one must first create the necessary user and DB table specified in the <code>application.properties</code>, and grant the user access and modification privileges to said DB table.
</summary>

Open the MySQL console in your terminal by typing: `sudo mysql -u root -p`

### Step 1/5: Configure Database 
- **Timezone**
   ```mysql
   -- set the timezone
   SET GLOBAL time_zone = '+00:00';
   -- verify it with
   SELECT @@global.time_zone, @@session.time_zone;
   -- You should see something like:
   -- +--------------------+---------------------+
   -- | @@global.time_zone | @@session.time_zone |
   -- +--------------------+---------------------+
   -- | +00:00             | SYSTEM              |
   -- +--------------------+---------------------+
   ```
- **Maximum `group_concat`**
  
   Also we need to increase the maximum length of `CONCAT_GROUP` statement results (by default is 1024 characters):
   ```mysql
   SET GLOBAL group_concat_max_len = 10000;
   ```

Note that this step is **necessary** whenever a new MySQL session starts, as the `SET` command will only be valid for the current session. 
To permanently set MySQL's configuration, you must add the following line to your `my.cnf` config file (Locate it with `mysql --help | grep /my.cnf`), under the `[mysqld]` section:
```cnf
[mysql]
default-time-zone = "+00:00"
group_concat_max_len=10000
```

Be sure to restart your MySQL service for the changes to take effect!

### Step 2/5: Create Database: `gse`

Create the database for the project by running:
``` mysql
CREATE DATABASE gse CHARACTER SET utf8 COLLATE utf8_bin;
```

### Step 3/5: Create User: `gseadmin`

Create the user by running these two commands in sequence:  
``` mysql
CREATE USER 'gseadmin'@'%' identified by 'Lugano2020';
GRANT ALL ON gse.* to 'gseadmin'@'%';
```

**Note**: The `gseadmin` user is only required for the flyway migrations, as well as for JPA to access the database.


### Step 4/5: Create Tables
Create tables:
```shell
$ mysql -u gseadmin -pLugano2020 gse < ./docker-compose/initdb/1-gse-db-schema.sql
```

### Step 5/5: Populate Tables (Optional)
Initialize the database with an existing dataset of mined repositories — or otherwise the Crawler will start from scratch.
```shell
$ mysql -u gseadmin -pLugano2020 gse < ./docker-compose/initdb/2-gse-db-data-***.sql
```
</details>

## 2. Setup Crawler
To make Crawler work, you have to initialize `supported_language` and  `access_token`. For that, you have two options:
1. Providing languages/tokens when populating the database (as part of `docker-compose/initdb/2-gse-db-data***.sql` dump) (**Recommended**)
2. Manually adding to the tables.
   ```shell
   $ mysql -u gseadmin -pLugano2020 gse
   > [same INSERT statements in next(3) option]
   ```
3. Creating a migration file at `resources/db/migration/V0__initialize_tokens_languages.sql` and run the app:
   ```sql
   -- Initialize crawler programming langauges
   INSERT INTO supported_language VALUES (1,'Java', current_timestamp);
   INSERT INTO supported_language VALUES (2,'Kotlin', current_timestamp);
   INSERT INTO supported_language VALUES (3,'C', current_timestamp);
   INSERT INTO supported_language VALUES (4,'C++', current_timestamp);
   INSERT INTO supported_language VALUES (5,'C#', current_timestamp);
   INSERT INTO supported_language VALUES (6,'Python', current_timestamp);
   INSERT INTO supported_language VALUES (7,'JavaScript', current_timestamp);
   INSERT INTO supported_language VALUES (8,'TypeScript', current_timestamp);
   INSERT INTO supported_language VALUES (9,'Swift', current_timestamp);
   INSERT INTO supported_language VALUES (10,'Objective-C', current_timestamp);
   -- Initialize GitHub access tokens
   INSERT INTO access_token (value,added) VALUES ('<YOUR_GITHUB_ACCESS_TOKEN>',current_timestamp);
   ```
   
## 3. Running the `application`

### I. Running in IntelliJ

This is my preferred method for running and testing the application as it allows for easy debugging. 
If the run configuration has not been set, then navigate to `usi/si/seart/GSEApplication.java`. The option to start the application from the main method should be on the left hand side, next to the class definition. After running for the first time, the configuration should get automatically saved to the list of available configurations. It is not necessary to provide any arguments, as Spring takes the default ones from application.properties. If you wish to override any of the arguments, it is as simple as changing them in the application.properties file.  

### II. Running in the terminal


1. Make sure Apache Maven (`mvn`) is installed

    <details>
    <summary>How to install Maven?</summary>
    
    1. First downloaded the latest version of [Apache Maven](https://maven.apache.org/download.cgi).
    2. Next, add the `apache-maven-X.X.X/bin` to `PATH` environment variable
       ```shell
       # add this to ~/.zshrc or ~/.bash_profile
       export PATH="/usr/local/apache-maven-x.x.x/bin/:$PATH"
       ```
    3. To ensure that the path variable has been added, run: `mvn -v`
    </details>

2. Navigate to the root folder of the project. To run the application with the default parameters (specified in the `application.properties` file), simply run:
    ```shell
    mvn spring-boot:run
    ```
3. And to override the value of an existing parameter, run:
    ```shell
    mvn spring-boot:run -Dspring-boot.run.arguments=--arg.one.name=argvalue,--arg.two.name=1
    ```

### III. Running using `.jar`

1. Make sure Apache Maven (`mvn`) is installed (How to install? see the previous section)
2. Build the project: `mvn clean package`. If it did not exist yet, you should now see the `target` directory in the project root.
3. Navigate to the root folder of the project. To run the application with the default parameters (specified in the `application.properties` file), simply run:
   ```shell
   java -jar target/gse-application-X.X.X.jar 
   ```
4. And to override the value of an existing parameter, run:
   ```shell
   java -Dapp.crawl.enabled=false -jar target/gse-application-x.x.x.jar
   ```
- Note that the name of the `.jar` file **`gse-application-x.x.x`** is derived from the settings in `pom.xml`, following the format of: `artifactId-version.jar`.

### Supported arguments

Here's a list of arguments supported by the application that you can find in `application.properties` (usual place for _Spring_ projects):

| variable name | type | default value | description |
| ------------- | ---- | ------------- | ----------- |
|`app.crawl.enabled`|boolean|true|Specifies if the crawling jobs are enabled on startup|
| `app.crawl.scheduling` | String | 21600000 (6h, in ms) | Scheduling rate, expressed as a numeric string |
| `app.crawl.startdate` | String | 2008-01-01T00:00:00 | "Beginning of time". Basically the earliest supported date for crawling repos, if no crawl jobs were previously performed. Formatted as a yyyy-MM-ddTHH:MM:SS string. |
  
Note that although there are other parameters, I strongly recommend you **DON'T** override them.


## 4. Running the `front-end`

The easiest way to start the front-end is through IntelliJ itself. After starting the application back-end, navigate to `src/main/fe-src` in the project tree. Right click on `index.html`, and select one of the provided launch options from `Open In Browser`. Please note that IntelliJ's built-in web-server port is [by default configured](https://www.jetbrains.com/help/idea/php-built-in-web-server.html#configuring-built-in-web-server) to `63342`. In order to access the back-end API, change it to `3030` in `Preferences > Build, Execution, Deployment > Debugger > Built-in server`, as the application CORS configurer can only accept connections from `localhost:3030`.
