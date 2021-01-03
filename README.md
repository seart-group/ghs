
# GHSearch Platform

This project is composed of two main subprojects:
1. `application`: The main application has two main responsibilities:
    1. Crawling GitHub and retrieving repository information. This can be disabled with `app.crawl.enabled` argument.
    2. Serving as the backend server for frontend queries.
2. `front-end`: This is where users can search for repositories.

## Installation and Usage

The detailed instruction can be find [here](./RUN.md)


## Dockerisation :whale:

To dockerise the application, I have opted to use three containers:

| Service name | Container name | Goal |
| ------------ | -------------- | ---- |
| `gse-app` | `gse-application` | for the spring application itself |
| `gse-fe` | `gse-frontend` | for supplying the front end files |
| `mysql` | `gse-database` | for the database |

The docker-compose configuration `docker-compose.yml` is configured to automatically run scripts to import the previous backup of the database.

### Build and Deploy

To deploy a back-end version, be sure to build a clean project jar: `mvn clean package`

After that, deploy using the following commands:
```
docker build -t gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/backend -f Dockerfile.be .
docker push gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/backend
```

To push front-end images, simply run:
```
docker build -t gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/frontend -f Dockerfile.fe .
docker push gitlab.reveal.si.usi.ch:60090/students/2020/ozren-dabic/github-search-engine/frontend
```

On server side:
1. Fetch new image(s): `docker-compose pull`
2. The, run it: `gse-compose up -d <service-name>` where service-names are `gse-fe`, `gse-app` and `mysql`


## More Info on Flyway and Database Migration
Read [here](./README_flyway.md)

## FAQ
- **How add a new programming language to platform?** See [this commit for adding C#](https://gitlab.reveal.si.usi.ch/devinta/github-search-engine/-/commit/2fd9c1da171119f5d33fd157b2275ad6429264ce) on 17th Decemeber 2020.

## Important TODOs
- [ ] Current *Advance Search* re-implementation via Native SQL query may be subject to SQL Injection.
