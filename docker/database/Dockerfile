FROM mysql:8.3.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/ukb7jwhbfljp4e8lteqyr/gse.sql.gz?rlkey=q8p5tsj93vz27nmxcelb6ccl2&st=0niplq73&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
