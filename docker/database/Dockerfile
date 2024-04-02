FROM mysql:8.3.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/5eku4c65on1g26rnjzsx1/gse.sql.gz?rlkey=08l0sfvyj29b2wfvzw5vuuay4&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
