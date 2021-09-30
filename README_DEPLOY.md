# Dockerisation :whale:

GHS service is composed of three containers:

| Service name | Container name | Goal |
| ------------ | -------------- | ---- |
| `gse-app` | `gse-app` | for the spring application itself |
| `gse-fe` | `gse-fe` | for supplying the front end files |
| `gse-db` | `gse-db` | for the database |

The docker-compose configuration `docker-compose.yml` is configured to automatically run scripts to import the previous backup of the database.

## Build and Deploy

1. Configure Crawler with GitHub access token and programming languages (See [Here](./README_SETUP.md#2-setup-crawler))

2. Build the backend as `jar` file:
```shell
mvn clean package
```

3. To deploy back-end image use the following commands (the tailing `.` should refers to root of the project):
```shell
docker build -t ghs-backend:latest -f docker/Dockerfile.be .
```

4. To deploy front-end images, simply run (the tailing `.` should refer to the root of the project):
```shell
docker build -t ghs-frontend:latest -f docker/Dockerfile.fe .
```

5. On server side, or the machine you want to deploy on:
    1. (first time only) Copy `docker-compose` folder on your server.
    2. Fetch the new image(s) you built in the previous step: `docker-compose pull`
    3. The, run it: `docker-compose up`
       - If you're running without a reverse-proxy server, read comments on top of `docker-compose.yml` file

**Note:** To override default parameters set in `application.properties`, you can define enviroments in `docker-compose.yml`. For instance to disable crawling ([src](https://stackoverflow.com/questions/46057625)):
   ```yml
   services:
     gse-app:
        environment:
          # will override default parameters as "app.crawl.enabled=false" 
          APP_CRAWL_ENABLED: 'false' 
   ```
