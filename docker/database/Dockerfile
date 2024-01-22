FROM mysql:8.3.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/rhsk7wac5zr6eulqv9872/gse.sql.gz?rlkey=bfvbel3am8mrxf1ce8e9ijl75&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
