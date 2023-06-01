FROM maven:3.8.6-jdk-11-slim AS build

COPY ./pom.xml /pom.xml
COPY ./src ./src

RUN mvn -e --no-transfer-progress clean install -am -DskipTests

FROM adoptopenjdk/openjdk11:jre-11.0.19_7-alpine

COPY --from=build /target/ghs-application-*.jar /server.jar

RUN apk update && apk add --no-cache \
                      bash \
                      git \
                      cloc \
                      curl

EXPOSE 8080

ENTRYPOINT java -jar server.jar
