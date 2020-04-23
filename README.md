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

### Running in IntelliJ

This is my preferred method for running and testing the application as it allows for easy debugging. If the run configuration has not been set, then navigate to **com.dabico.gseapp.GSEApplication.java**. The option to start the application from the main method should be on the left hand side, next to the class definition. After running for the first time, the configuration should get automatically saved to the list of available configurations. It is not necessary to provide any arguments, as Spring takes the default ones from application.properties. If you wish to override any of the arguments, it is as simple as changing them in the application.properties file.  

### Running in the terminal

To run the application through the terminal, first make sure you have downloaded the latest version of [Apache Maven](https://maven.apache.org/download.cgi). Next, add the **bin** directory of **apache-maven-X.X.X** to the PATH environment variable. So for example, if I put it in my Documents folder, then to add the environment variable I would run:
```
export PATH=/Users/username/Documents/apache-maven-X.X.X/bin:$PATH
```
Note that this will only temporarily add the environment variable, until the current terminal is ends. Refer to [this article](https://medium.com/@youngstone89/setting-up-environment-variables-in-mac-os-28e5941c771c) if you wish to permanently add it. To ensure that the path variable has been added, run:  
```
mvn -v
```
If it runs without error, and prints the version installed, along with other details, then maven is successfully installed and the application is ready for use. Navigate to the root folder of the project. To run the application with the default parameters specified in the application.properties file, simply run:
```
mvn spring-boot:run
```
And to override the value of an existing parameter, run:
```
mvn spring-boot:run -Dspring-boot.run.arguments=--arg.one.name=argvalue,--arg.two.name=1
```

### Supported arguments

Here's a list of arguments supported by the application:
1. app.crawl.enabled
  - Type: boolean
  - Default: true
  - Description: Specifies if the crawling jobs are enabled on startup
2. app.crawl.scheduling
  - Type: String
  - Default: 43200000 (12H translated to MS)
  - Description: Scheduling rate, expressed as a numeric string
3. app.crawl.startdate
  - Type: String
  - Default: 2008-01-01T00:00:00
  - Description: "Beginning of time", basically the earliest date for crawling repos, formatted as a yyyy-MM-ddTHH:MM:SS string.
  
Note that although there are other parameters, I strongly recommend you **DON'T** override them.