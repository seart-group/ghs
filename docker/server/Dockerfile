FROM maven:3.9.3-eclipse-temurin-17-alpine AS build

COPY ./pom.xml /pom.xml
COPY ./src ./src

RUN mvn -e --no-transfer-progress clean install -am -DskipTests

FROM eclipse-temurin:19.0.2_7-jre-alpine

COPY --from=build /target/ghs-application-*.jar /server.jar

RUN apk update --quiet
RUN apk add --no-cache \
            --quiet \
            git \
            cloc \
            curl

EXPOSE 8080

ENTRYPOINT java -jar server.jar
