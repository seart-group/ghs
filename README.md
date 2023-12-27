# GitHub Search &middot; [![Status](https://badgen.net/https/dabico.npkn.net/ghs-status)](http://seart-ghs.si.usi.ch) [![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/seart-group/ghs/blob/master/LICENSE) [![Latest Dump](https://img.shields.io/badge/Latest_Dump-16.12.23-blue)](https://www.dropbox.com/scl/fi/833d78twgzdma1hd1r19l/gse.sql.gz?rlkey=qovbf5dozvrdpt40w6gm5ezql&dl=1) [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.4588464.svg)](https://doi.org/10.5281/zenodo.4588464)

This project is made of two components:
1. A Spring Boot powered back-end, responsible for:
    1. Continuously crawling GitHub API endpoints for repository information, and storing it in a central database;
    2. Acting as an API for providing access to the stored data.
2. A Bootstrap-styled and jQuery powered web user interface, serving an accessible front for the API, available at http://seart-ghs.si.usi.ch

## Running Locally

### Prerequisites

| Dependency                               | Version Requirement |
|------------------------------------------|--------------------:|
| Java                                     |                  17 |
| Maven                                    |               3.9.3 |
| MySQL                                    |               8.2.0 |
| Flyway                                   |              10.4.1 |
| Git                                      |              2.25.2 |
| [cloc](https://github.com/AlDanial/cloc) |                1.96 |

### Database

Before choosing whether to start with a clean slate or pre-populated database, make sure the following requirements are met:

1. The database timezone is set to `+00:00`. You can verify this via:

    ```sql
    SELECT @@global.time_zone, @@session.time_zone;
    ```

2. The event scheduler is turned `ON`. You can verify this via:

   ```sql
   SELECT @@global.event_scheduler;
   ```

3. The binary logging during the creation of stored functions is set to `1`. You can verify this via:

    ```sql
    SELECT @@global.log_bin_trust_function_creators;
    ``` 

4. The `gse` database exists. To create it:

    ```sql
    CREATE DATABASE gse CHARACTER SET utf8 COLLATE utf8_bin;
    ```

5. The `gseadmin` user exists. To create one, run:

    ```sql
    CREATE USER IF NOT EXISTS 'gseadmin'@'%' IDENTIFIED BY 'Lugano2020';
    GRANT ALL ON gse.* TO 'gseadmin'@'%';
    ```

If you want to start with a completely blank database, then no further action is required.
The necessary tables will be created by virtue of Flyway migrations, which will run on initial server startup.
However, if you want your local database to be pre-initialized with the data we have mined, then you can use the compressed SQL dump we provide.
We host said dump, as well as the previous 4 iterations, on [Dropbox](https://www.dropbox.com/scl/fo/lqvp1mhsg0ezp2sgs0xdk/h?rlkey=j9joij3iqpy1zl5h061vdnlj6).
Once you select and download a database dump, you can import the data by running:

```shell
gzcat < gse.sql.gz | mysql -u gseadmin -pLugano2020 gse
```

### Server

Before attempting to run the server, you must generate your own GitHub personal access token (PAT).
GHS relies on the GraphQL API, which is inaccessible without authentication. 
The token must include the `repo` scope, in order for it to access the information present in the GitHub API.

Once that is done, you can run the server locally using Maven:

```shell
mvn spring-boot:run
```

If you want to make use of the token when crawling, specify it in the run arguments:

```shell
mvn spring-boot:run -Dspring-boot.run.arguments=--ghs.github.tokens=<your_access_token>
```

Alternatively, you can compile and run the JAR directly:

```shell
mvn clean package
ln target/ghs-application-*.jar target/ghs-application.jar
java -Dghs.github.tokens=<your_access_token> -jar target/ghs-application.jar
```

Here's a list of project-specific arguments supported by the application that you can find in the `application.properties`:

| Variable Name                                 | Type               | Default Value                                                           | Description                                                                                                                                                                                                                                                        |
|-----------------------------------------------|--------------------|-------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ghs.github.tokens`                           | List&lt;String&gt; |                                                                         | List of [GitHub personal access tokens (PATs)](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) that will be used for mining the GitHub API. Must not contain blank strings.                   |
| `ghs.github.api-version`                      | String             | 2022-11-28                                                              | [GitHub API version](https://docs.github.com/en/rest/overview/api-versions) used across various operations.                                                                                                                                                        |
| `ghs.crawler.enabled`                         | Boolean            | true                                                                    | Specifies if the repository crawling job is enabled.                                                                                                                                                                                                               |
| `ghs.crawler.minimum-stars`                   | int                | 10                                                                      | Inclusive lower bound for the number of stars a project needs to have in order to be picked up by the crawler. Must not be negative.                                                                                                                               |
| `ghs.crawler.languages`                       | List&lt;String&gt; | See [application.properties](src/main/resources/application.properties) | List of language names that will be targeted during crawling. Must not contain blank strings. To ensure proper operations, the names must match those specified in [linguist](https://github.com/github-linguist/linguist/blob/master/lib/linguist/languages.yml). |
| `ghs.crawler.start-date`                      | Date               | 2008-01-01T00:00:00Z                                                    | Default crawler start date: the earliest date for repository crawling in the absence of prior crawl jobs. Value format: `yyyy-MM-ddTHH:MM:SSZ`.                                                                                                                    |
| `ghs.crawler.delay-between-runs`              | Duration           | PT6H                                                                    | Delay between successive crawler runs, expressed as a duration string.                                                                                                                                                                                             |
| `ghs.analysis.enabled`                        | Boolean            | true                                                                    | Specifies if the analysis job is enabled.                                                                                                                                                                                                                          |
| `ghs.analysis.delay-between-runs`             | Duration           | PT6H                                                                    | Delay between successive analysis runs, expressed as a duration string.                                                                                                                                                                                            |
| `ghs.analysis.max-pool-threads`               | int                | 3                                                                       | Maximum amount of live threads dedicated to concurrently analyzing repositories. Must be positive.                                                                                                                                                                 |
| `ghs.analysis.git.folder-prefix`              | String             | ghs-clone-                                                              | Prefix used for the temporary directories into which analyzed repositories are cloned. Must not be blank.                                                                                                                                                          |
| `ghs.analysis.git.clone-timeout-duration`     | Duration           | 5m                                                                      | Maximum time allowed for cloning Git repositories.                                                                                                                                                                                                                 |
| `ghs.analysis.cloc.analysis-timeout-duration` | Duration           | 5m                                                                      | Maximum time allowed for analyzing cloned Git repositories with `cloc`.                                                                                                                                                                                            |
| `ghs.clean-up.enabled`                        | Boolean            | true                                                                    | Specifies if the job responsible for removing unavailable repositories (clean-up) is enabled.                                                                                                                                                                      |
| `ghs.clean-up.cron`                           | CronTrigger        | 0 0 0 * * 1                                                             | Delay between successive repository clean-up runs, expressed as a [Spring CRON expression](https://spring.io/blog/2020/11/10/new-in-spring-5-3-improved-cron-expressions).                                                                                         |
| `ghs.clean-up.curl.connect-timeout-duration`  | Duration           | 1m                                                                      | Maximum time allowed for establishing HTTP connections with `curl`.                                                                                                                                                                                                |
| `ghs.statistics.suggestion-limit`             | int                | 500                                                                     | Maximum number of suggestions available in UI autocompletion. Must not be negative. To disable the limit use 0.                                                                                                                                                    |

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

| Service/Container name |                                  Image                                  | Description                              |      Enabled by Default       |
|------------------------|:-----------------------------------------------------------------------:|------------------------------------------|:-----------------------------:|
| `gse-database`         |            [mysql](https://registry.hub.docker.com/_/mysql)             | Platform database                        |      :white_check_mark:       |
| `gse-migration`        |        [flyway](https://registry.hub.docker.com/r/flyway/flyway)        | Database schema migration executions     |      :white_check_mark:       |
| `gse-backup`           |  [tiredofit/db-backup](https://hub.docker.com/r/tiredofit/db-backup/)   | Automated database backups               | :negative_squared_cross_mark: |
| `gse-server`           |              [seart/ghs-server](docker/server/Dockerfile)               | Spring Boot server application           |      :white_check_mark:       |
| `gse-website`          |             [seart/ghs-website](docker/website/Dockerfile)              | NGINX web server acting as HTML supplier |      :white_check_mark:       |
| `gse-watchtower`       | [containrrr/watchtower](https://hub.docker.com/r/containrrr/watchtower) | Automatic Docker image updates           | :negative_squared_cross_mark: |

The service dependency chain can be represented as follows:

```mermaid
graph RL
    gse-migration --> |service_healthy| gse-database
    gse-backup --> |service_completed_successfully| gse-migration
    gse-server --> |service_completed_successfully| gse-migration
    gse-website --> |service_healthy| gse-server
    gse-watchtower --> |service_started| gse-website
```

Deploying is as simple as, in the [docker-compose](docker-compose) directory, run:

```shell
docker-compose -f docker-compose.yml up -d
```

It's worth mentioning that the database setup steps outlined in the previous section are not needed when running with docker,
as the environment properties passed to the service will create the user and DB on first ever startup.
However, the same does not apply to the database data, as the default deployment will create an empty database.
If you want to use existing data from the dumps, then you have to override the compose deployment to use a custom database image that comes bundled with the dump.
Create your `docker-compose.override.yml` file, and add to it the following contents:

```yaml
version: '3.9'
name: 'gse'

services:

  gse-database:
     image: seart/ghs-database:latest
```

The above image will include the freshest database dump, at most 15 days behind the actual platform data.
For a more specific database version, refer to the [Docker Hub page](https://hub.docker.com/r/seart/ghs-database/tags).
Just remember to specify the override file during deployment:

```shell
docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d
```

The database data itself is kept in the `gse-data` volume, while detailed back-end logs are kept in a local mount called [logs](docker-compose/logs).

You can also use this override file to change the configurations of other services, for instance specifying your own PAT for the crawler:

```yaml
version: '3.9'
name: 'gse'

services:

  # other services omitted...

  gse-server:
    environment:
      GHS_GITHUB_TOKENS: 'A single or comma-separated list of token(s)'
      GHS_CRAWLER_ENABLED: 'true'
```

Any of the Spring Boot properties or aforementioned application-specific properties can be overridden.
Just keep in mind that `ghs.x.y` corresponds to the `GHS_X_Y` service environment setting.

Another example is the automated database backup service, which is disabled by default.
Should you chose to re-enable it, you would have to add the following to the override file:

```yaml
version: '3.9'
name: 'gse'

services:

  # other services omitted...

  gse-backup:
    restart: always
    entrypoint: "/init"
```

---

## FAQ

### How can I report a bug or request a feature or ask a question?

Please add a [new issue](https://github.com/seart-group/ghs/issues/), and we will get back to you very soon.

### How do I extend/modify the existing database schema?

In order to do that, you should be familiar with database migration tools and practices.
This project in particular uses [Flyway](https://flywaydb.org/) by Redgate.
However, the general rule for schema manipulation is: create new migrations, and _do not_ edit existing ones.
