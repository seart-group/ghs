FROM maven:3.9.2-eclipse-temurin-17-alpine AS build

COPY ./pom.xml /pom.xml
COPY ./src ./src

RUN mvn -e --no-transfer-progress clean package -am -DskipTests

FROM eclipse-temurin:17.0.7_7-jre-alpine

COPY --from=build /target/ghs-application-*.jar /server.jar

RUN apk update && apk add git curl cloc bash

EXPOSE 8080

ENTRYPOINT java -jar server.jar
