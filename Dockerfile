FROM markhobson/maven-chrome:jdk-13

WORKDIR /usr/local/

COPY . .

ENTRYPOINT ["java","-jar","target/gse-application-0.9.7.jar"]
