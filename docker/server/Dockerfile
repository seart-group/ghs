FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /

COPY .git .git/
COPY src src/
COPY pom.xml pom.xml

RUN mvn -e --no-transfer-progress clean package -am -DskipTests

FROM eclipse-temurin:17.0.10_7-jre-alpine
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

COPY --from=build /target/ghs-application-*.jar /server.jar

RUN apk update --quiet && \
    apk add --no-cache \
            --quiet \
            cloc~=1.98 \
            curl~=8.5.0 \
            git~=2.43.0 \
            jq~=1.7.1

ENTRYPOINT ["java", "-jar", "server.jar"]
