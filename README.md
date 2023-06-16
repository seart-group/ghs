
# GitHub Search &middot; [![Status](https://badgen.net/https/dabico.npkn.net/ghs-status)](http://seart-ghs.si.usi.ch) [![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/seart-group/ghs/blob/master/LICENSE) [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.4588464.svg)](https://doi.org/10.5281/zenodo.4588464)

This project is made of two components:
1. A Spring Boot powered back-end, responsible for:
    1. Continuously crawling GitHub API endpoints for repository information, and storing it in a central database;
    2. Acting as an API for providing access to the stored data.
2. A Bootstrap-styled and jQuery powered web user interface, serving an accessible front for the API, available at http://seart-ghs.si.usi.ch

## Running Locally

### Prerequisites

- Java 11
- Maven (3.8+)
- MySQL (8.0.32+)
- Git

### Database

Before choosing whether to start with a clean slate or pre-populated database, make sure the following requirements are met:

1. The database timezone is set to UTC (+00:00). You can verify this via:

    ```sql
    SELECT @@global.time_zone, @@session.time_zone;
    ```

2. The `gse` database exists. To create it:

    ```sql
    CREATE DATABASE gse CHARACTER SET utf8 COLLATE utf8_bin;
    ```

3. The `gseadmin` user exists. To create one, run:

    ```sql
    CREATE USER IF NOT EXISTS 'gseadmin'@'%' IDENTIFIED BY 'Lugano2020';
    GRANT ALL ON gse.* TO 'gseadmin'@'%';
    ```

If you want to start with a completely blank database, then no further action is required.
The necessary tables will be created by virtue of Flyway migrations, which will run on initial server startup.
However, if you want your local database to be pre-initialized with the data we have mined, then you can use the compressed SQL dump we provide.
Said dump can be found in [docker-compose/initdb](docker-compose/initdb), and to import it you would run:

```shell
gzcat < docker-compose/initdb/gse.sql.gz | mysql -u gseadmin -pLugano2020 gse
```

### Server

Before attempting to run the server, I advise you generate your own GitHub personal access token (PAT).
Said token should include the `repo` scope, in order for it to effectively crawl the GitHub API.
While the token is not mandatory, the impact its presence has on the mining speed can not be understated.

Once that is done, you can run the server locally using Maven:

```shell
mvn spring-boot:run
```

If you want to make use of the token when crawling, specify it in the run arguments:

```shell
mvn spring-boot:run -Dspring-boot.run.arguments=--app.crawl.tokens=<your_access_token>
```

Alternatively, you can compile and run the JAR directly:

```shell
mvn clean package
ln target/ghs-application-*.jar target/ghs-application.jar
java -Dapp.crawl.tokens=<your_access_token> -jar target/ghs-application.jar
```

Here's a list of project-specific arguments supported by the application that you can find in the `application.properties`:

| variable name                | type               | default value                                                           | description                                                                                                                                                           |
|------------------------------|--------------------|-------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `app.crawl.enabled`          | boolean            | true                                                                    | Specifies if the crawling jobs are enabled on startup                                                                                                                 |
| `app.crawl.languages`        | List&lt;String&gt; | See [application.properties](src/main/resources/application.properties) | A comma-separated list of language names that will be targeted during crawling                                                                                        |
| `app.crawl.tokens`           | List&lt;String&gt; |                                                                         | A comma-separated list of GitHub personal access tokens (PATs) that will be used for mining the GitHub API                                                            |
| `app.crawl.scheduling`       | String             | 21600000 (6h, in ms)                                                    | Crawler scheduling rate, expressed as a numeric string                                                                                                                |
| `app.crawl.startdate`        | String             | 2008-01-01T00:00:00                                                     | "Beginning of time". Basically the earliest supported date for crawling repos, if no crawl jobs were previously performed. Formatted as a yyyy-MM-ddTHH:MM:SS string. |
| `app.cleanup.enabled`        | boolean            | true                                                                    | Specified if the job responsible for removing unavailable repositories is enabled on startup                                                                          |
| `app.cleanup.scheduling`     | String             | 21600000 (6h, in ms)                                                    | Cleanup scheduling rate, expressed as a numeric string                                                                                                                |
| `app.cache-evict.scheduling` | String             | 21600000 (6h, in ms)                                                    | Query cache eviction scheduling rate, expressed as a numeric string                                                                                                   |

### Web UI

The easiest way to start the front-end is through IntelliJ's built-in web server.
After starting the application back-end right click on `index.html` in the [html](html) directory,
and select one of the provided launch options from `Open In > Browser`.
Alternatively, you can self-host the web UI by virtue of tools such as `http-server`:

```shell
# install by running: npm install -g http-server
http-server html -p 3030
```

Regardless of which method you choose for hosting, the back-end CORS restricts you to using either port `3030` or `7030`.

## Dockerisation :whale:

The deployment stack consists of the following containers:

| Service/Container name |                      Image                       | Purpose                           |            Enabled            |
|------------------------|:------------------------------------------------:|-----------------------------------|:-----------------------------:|
| `gse-app`              |       [gse/backend](docker/Dockerfile.be)        | for the spring application itself |      :white_check_mark:       |
| `gse-fe`               |       [gse/frontend](docker/Dockerfile.fe)       | for supplying the front end files |      :white_check_mark:       |
| `gse-db`               | [mysql](https://registry.hub.docker.com/_/mysql) | for the database                  |      :white_check_mark:       |
| `gse-bkp`              |       [gse/backup](docker/Dockerfile.bkp)        | for the automatic backups         | :negative_squared_cross_mark: |

Deploying is as simple as, in the [docker-compose](docker-compose) directory, run:

```shell
docker-compose -f docker-compose.yml up -d
```

It's worth mentioning that the database setup steps outlined in the previous section are not needed when running with docker,
as the environment properties passed to the service will create the user and pre-populate the DB on first ever startup.
The database data itself is kept in the `gse-data` volume,
while detailed back-end logse are kept in a local mount called "logs" in [docker-compose](docker-compose).
    
The database backup service is disabled by default, as we use it primarily in production.
Should you chose to enable it, you would have to define your own personal override file.
Here's an example of a `docker-compose.override.yml` that re-enables backups:

```yaml
version: '3.9'
name: 'gse'

services:

  gse-bkp:
    restart: always
    entrypoint: "/init"
```

You can also use this override file to change the service configurations of other services,
for instance specifying your own PAT for the crawler:

```yaml
version: '3.9'
name: 'gse'

services:

   gse-app:
      environment:
         APP_CRAWL_ENABLED: 'true'
         APP_CRAWL_TOKENS: '<your_access_token>'
```

Any of the Spring Boot properties or aforementioned application-specific properties can be overridden.
Just keep in mind that `app.x.y` corresponds to the `APP_X_Y` service environment setting.
Don't forget to specify the override file when running the command:

```shell
docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d
```

---

## FAQ

### How can I report a bug or request a feature or ask a question?

Please add a [new issue](https://github.com/seart-group/ghs/issues/), and we will get back to you very soon.

### How do I extend/modify the existing database schema?

In order to do that, you should be familiar with database migration tools and practices.
This project in particular uses [Flyway](https://flywaydb.org/) by Redgate.
However, the general rule for schema manipulation is: create new migrations, and _do not_ edit existing ones.
