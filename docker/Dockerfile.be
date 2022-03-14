FROM openjdk:11.0.4-jre-slim

RUN apt-get -y update
RUN apt-get -y install git


COPY target /usr/local/target

WORKDIR /usr/local/

EXPOSE 8080

ENTRYPOINT ["/bin/bash", "-c", "sleep 30 && java -jar target/ghs-application-*.jar"]
