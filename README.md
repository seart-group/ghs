# Installation and Usage Guide

## Creating the necessary MySQL user and database table

For the project to work, one must first create the necessary user and DB table specified in the application.properties, and grant the user access and modification privileges to said DB table.  

### Step 1:

Open the MySQL console in your terminal by typing:
```
sudo mysql
```
After providing your password (provided you have one in place), you should be greeted with the MySQL console.  

### Step 2:

Create the database for the project by running:  
``` mysql
CREATE DATABASE gse CHARACTER SET utf8 COLLATE utf8_bin;
```

### Step 3:

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

### Step 4:

Create the user by running these two commands in sequence:  
``` mysql
CREATE USER 'gseadmin'@'%' identified by 'Lugano2020';
GRANT ALL ON gse.* to 'gseadmin'@'%';
```

If all the commands above worked, then your database should be ready for use.  
If for any reason whatsoever you wish to drop and create the database, then simply running:  
``` mysql
DROP DATABASE gse;
CREATE DATABASE gse CHARACTER SET utf8 COLLATE utf8_bin;
```
Will do that for you. Note that you do not have to run the MySQL console as the aforementioned *gseadmin* user. The user is only required for the flyway migrations, as well as for JPA to access the database.  

## Running the application

### I. Running in IntelliJ

This is my preferred method for running and testing the application as it allows for easy debugging. If the run configuration has not been set, then navigate to **com.dabico.gseapp.GSEApplication.java**. The option to start the application from the main method should be on the left hand side, next to the class definition. After running for the first time, the configuration should get automatically saved to the list of available configurations. It is not necessary to provide any arguments, as Spring takes the default ones from application.properties. If you wish to override any of the arguments, it is as simple as changing them in the application.properties file.  

### II. Running in the terminal

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

### III. Running using `.jar`

First build the project by running the following command in the terminal:
```
mvn clean package
```
If it did not exist yet, you should now see the **target** directory in the project root. The root of said directory will contain the Java Archive file for the project. All you have to do now is run the following:
```
java -jar target/gse-application-X.X.X.jar
```
Note that the name of the `.jar` file **`gse-application-X.X.X`** is derived from the settings in `pom.xml`, following the format of: `artifactId-version.jar`.

### Supported arguments

Here's a list of arguments supported by the application:
1. `app.crawl.enabled`
  - Type: boolean
  - Default: true
  - Description: Specifies if the crawling jobs are enabled on startup
2. `app.crawl.scheduling`
  - Type: String
  - Default: 43200000 (12H translated to MS)
  - Description: Scheduling rate, expressed as a numeric string
3. `app.crawl.startdate`
  - Type: String
  - Default: 2008-01-01T00:00:00
  - Description: "Beginning of time". Basically the earliest supported date for crawling repos, if no crawl jobs were previously performed. Formatted as a yyyy-MM-ddTHH:MM:SS string.
  
Note that although there are other parameters, I strongly recommend you **DON'T** override them.

## Starting the front-end

The easiest way to start the front-end is through IntelliJ itself. After starting the application back-end, navigate to `src/main/fe-src` in the project tree. Right click on `index.html`, and select one of the provided launch options from `Open In Browser`. Please note that IntelliJ's built-in web-server port is [by default configured](https://www.jetbrains.com/help/idea/php-built-in-web-server.html#configuring-built-in-web-server) to `63342`. In order to access the back-end API, change it to `3030` in `Preferences > Build, Execution, Deployment > Debugger > Built-in server`, as the application CORS configurer can only accept connections from `localhost:3030`.

## Dockerisation

To dockerise the application, I have opted to use three containers: one for the database, one for supplying the front end files, and one for the spring application itself. To deploy the application, simply run the following:
```
docker-compose up
```
Docker compose will take care of the rest. The command essentially starts the `mysql`, `gse-app` and `gse-fe` services in that particular sequence. Three containers will start: `gse-application`, `gse-database` and `gse-frontend`. The compose configuration `docker-compose.yml` is configured to automatically run scripts to import the previous backup of the database.

## Build and Deploy

To deploy a back-end version, be sure to build a clean project jar. After that, deploy using the following commands:
```
docker build -t gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/backend -f Dockerfile.be .
docker push gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/backend
```
Changes should take effect on every 5th minute of the hour, as per Watchtower configuration.

To push front-end images, simply run:
```
docker build -t gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/frontend -f Dockerfile.fe .
docker push gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/frontend
```
Changes should be visible almost instantly.


## More Info on Flyway and Database Migration
Read [here](./README_flyway.md)
