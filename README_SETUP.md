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
For the project to work, one must first create the necessary user and DB table specified in the application.properties, and grant the user access and modification privileges to said DB table.
</summary>

**Note**: You do not have to run the MySQL console as the aforementioned `gseadmin` user. The user is only required for the flyway migrations, as well as for JPA to access the database.

### Step 1/6:

Open the MySQL console in your terminal by typing:
```
sudo mysql
```
After providing your password (provided you have one in place), you should be greeted with the MySQL console.  

### Step 2/6:

Create the database for the project by running:  
``` mysql
CREATE DATABASE gse CHARACTER SET utf8 COLLATE utf8_bin;
```

### Step 3/6:

Manually set the MySQL server timezone to UTC, using the command:
```mysql
SET GLOBAL time_zone = '+00:00';
```
To ensure that the timezone has been successfully set, run:
```mysql
SELECT @@global.time_zone, @@session.time_zone;
```
You should see something like:
```
+--------------------+---------------------+
| @@global.time_zone | @@session.time_zone |
+--------------------+---------------------+
| +00:00             | SYSTEM              |
+--------------------+---------------------+
1 row in set (0.00 sec)
```
Note that this step is **necessary** whenever a new MySQL session starts, as the `SET` command will only be valid for the current session. To permanently set MySQL's default timezone to UTC, you must add the following line to your `my.cnf` (usually located in `/usr/local/etc`) config file, under the `[mysqld]` section:
```
default-time-zone = "+00:00"
```
Be sure to restart your MySQL service for the changes to take effect! If you are having trouble with locating the `my.cnf` file, type the following into the terminal:
```
mysql --help | grep /my.cnf
```

### Step 4/6:

Create the user by running these two commands in sequence:  
``` mysql
CREATE USER 'gseadmin'@'%' identified by 'Lugano2020';
GRANT ALL ON gse.* to 'gseadmin'@'%';
```

If all the commands above worked, then your database should be ready for use.  

If for any reason whatsoever you wish to drop and create the database, then simply run `DROP DATABASE gse;` and start from scratch.  


### Step 5/6
Create tables:
```shell
$ mysql -u gseadmin -p gse < docker-compose/initdb/1-gse-db-schema.sql`
```

### Step 6/6
(Optional) Initialize the database with an existing dataset of mined repositories — or otherwise the Crawler will start from scratch.
```shell
$ mysql -u gseadmin -p gse < docker-compose/initdb/2-gse-db-data-***.sql`
```

</details>

## 2. Setup Crawler
To make Crawler work, you have to initialize `supported_language` and  `access_token`. For that, you have two options:
1. Providing this information as part of your `docker-compose/initdb/2-gse-db-data***.sql` dump. (**Recommended**)
2. Manually adding rows to the tables.
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

### Initialize GitHub Access Tokens for Crawler
- Make sure to enter at least one valid GitHub access token by updating `V1__initialize_tokens.sql` file before running the app for the first time.
- Also specify the Crawler's programming languages in `V0__initialize_languages.sql`.

## 3. Running the `application`

### I. Running in IntelliJ

This is my preferred method for running and testing the application as it allows for easy debugging. If the run configuration has not been set, then navigate to **com.dabico.gseapp.GSEApplication.java**. The option to start the application from the main method should be on the left hand side, next to the class definition. After running for the first time, the configuration should get automatically saved to the list of available configurations. It is not necessary to provide any arguments, as Spring takes the default ones from application.properties. If you wish to override any of the arguments, it is as simple as changing them in the application.properties file.  

### II. Running in the terminal
<details>

<summary>Steps to run the project via Terminal</summary>

To run the application through the terminal, first make sure you have downloaded the latest version of [Apache Maven](https://maven.apache.org/download.cgi). Next, add the **bin** directory of **apache-maven-X.X.X** to the PATH environment variable. So for example, if I put it in my Documents folder, then to add the environment variable I would run:
```
export PATH=/Users/username/Documents/apache-maven-X.X.X/bin:$PATH
```
Note that this will only temporarily add the environment variable, until the current terminal is ends. To permanently add it, simply run the following:
```
echo 'export PATH="/Users/username/Documents/apache-maven-X.X.X/bin:$PATH"' >> ~/.bash_profile
```
To ensure that the path variable has been added, run:  
```
mvn -v
```
If it runs without error, and prints the version installed, along with other details, then maven is successfully installed and the application is ready for use. Navigate to the root folder of the project. To run the application with the default parameters specified in the `application.properties` file, simply run:
```
mvn spring-boot:run
```
And to override the value of an existing parameter, run:
```
mvn spring-boot:run -Dspring-boot.run.arguments=--arg.one.name=argvalue,--arg.two.name=1
```
</details>

### III. Running using `.jar`

<details>

<summary>Steps to run the project using `.jar`</summary>

First build the project by running the following command in the terminal:
```
mvn clean package
```
If it did not exist yet, you should now see the **target** directory in the project root. The root of said directory will contain the Java Archive file for the project. All you have to do now is run the following:
```
java -jar target/gse-application-X.X.X.jar
```
Note that the name of the `.jar` file **`gse-application-X.X.X`** is derived from the settings in `pom.xml`, following the format of: `artifactId-version.jar`.
</details>

### Supported arguments

Here's a list of arguments supported by the application that you can find in `application.properties` file:

| variable name | type | default value | description |
| ------------- | ---- | ------------- | ----------- |
|`app.crawl.enabled`|boolean|true|Specifies if the crawling jobs are enabled on startup|
| `app.crawl.scheduling` | String | 21600000 (6h, in ms) | Scheduling rate, expressed as a numeric string |
| `app.crawl.startdate` | String | 2008-01-01T00:00:00 | "Beginning of time". Basically the earliest supported date for crawling repos, if no crawl jobs were previously performed. Formatted as a yyyy-MM-ddTHH:MM:SS string. |
  
Note that although there are other parameters, I strongly recommend you **DON'T** override them.


## 4. Running the `front-end`

The easiest way to start the front-end is through IntelliJ itself. After starting the application back-end, navigate to `src/main/fe-src` in the project tree. Right click on `index.html`, and select one of the provided launch options from `Open In Browser`. Please note that IntelliJ's built-in web-server port is [by default configured](https://www.jetbrains.com/help/idea/php-built-in-web-server.html#configuring-built-in-web-server) to `63342`. In order to access the back-end API, change it to `3030` in `Preferences > Build, Execution, Deployment > Debugger > Built-in server`, as the application CORS configurer can only accept connections from `localhost:3030`.
