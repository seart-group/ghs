FROM mysql:8.3.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/8l8w2et56kz1w0e1o3n7e/gse.sql.gz?rlkey=pu267yxp8496s6notn1rlmy1k&st=52lox7f1&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
