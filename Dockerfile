FROM openjdk:14-jdk-oracle

RUN apt-get update && apt-get install -y default-mysql-server

RUN test -e /var/run/mysqld || install -m 755 -o mysql -g root -d /var/run/mysqld
RUN chown -R mysql:mysql /var/lib/mysql /var/run/mysqld && service mysql start && \
    mysql -uroot -e "CREATE DATABASE gse CHARACTER SET utf8 COLLATE utf8_bin;" && \
    mysql -uroot -e "SET GLOBAL time_zone = '+00:00';" && \
    mysql -uroot -e "CREATE USER 'gseadmin'@'%' IDENTIFIED BY 'Lugano2020';" && \
    mysql -uroot -e "GRANT ALL ON gse.* TO 'gseadmin'@'%';"

ARG JAR_FILE=target/gse-application-0.9.7.jar

WORKDIR /usr/src/app

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","app.jar"]
