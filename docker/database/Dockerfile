FROM mysql:8.2.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/tz625z1gy450t2amby332/gse.sql.gz?rlkey=qfye5s1jyyvmcgc5h8wxr8a2g&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
