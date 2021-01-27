
# GHSearch Platform

This project is made of two subprojects:
1. `application`: The main application has two main responsibilities:
    1. Crawling GitHub and retrieving repository information. This can be disabled with `app.crawl.enabled` argument.
    2. Serving as the backend server for website/frontend
2. `front-end`: A frontend for searching the database (http://seart-ghs.si.usi.ch)

## Setup & Run Project Locally (for development)

The detailed instruction can be find [here](./RUN.md)


## Dockerisation :whale:

GHS service is composed of three containers:

| Service name | Container name | Goal |
| ------------ | -------------- | ---- |
| `gse-app` | `gse-app` | for the spring application itself |
| `gse-fe` | `gse-fe` | for supplying the front end files |
| `gse-db` | `gse-db` | for the database |

The docker-compose configuration `docker-compose.yml` is configured to automatically run scripts to import the previous backup of the database.

### Build and Deploy

1. (only first time) Make sure to initialize GitHub access tokens by updating `V1__initialize_tokens.sql` file and 
   specify the crawler programming languages on `V0__initialize_languages.sql`.
   - This should be done only for the first time. Once the migration is done by Flyway, this file should not be touched.

2. Build the backend as `jar` file: `mvn clean package`

3. To deploy back-end image use the following commands:
```
docker build -t gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/backend -f Dockerfile.be .
docker push gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/backend
```

4. To deploy front-end images, simply run:
```
mvn clean package
docker build -t gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/frontend -f Dockerfile.fe .
docker push gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/frontend
```

5. On server side:
   1. (first time only) Copy `docker` folder on your server.
   2. (first time only) Login: `docker login -u emadpres https://gitlab.reveal.si.usi.ch:60090`
   3. Fetch new image(s): `docker-compose pull`
   4. The, run it: `docker-compose up`


## More Info on Flyway and Database Migration
Read [here](./README_flyway.md)

## FAQ
- **How add a new programming language to platform?** See [this commit for adding C#](https://gitlab.reveal.si.usi.ch/devinta/github-search-engine/-/commit/2fd9c1da171119f5d33fd157b2275ad6429264ce) on 17th December 2020.

## Important TODOs
- [ ] Current *Advance Search* re-implementation via Native SQL query may be subject to SQL Injection.
