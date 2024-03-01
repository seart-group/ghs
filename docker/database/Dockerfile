FROM mysql:8.3.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/npdntnekzdpvjr62c3sq3/gse.sql.gz?rlkey=99wm57cqaq38oykd1c0r7rglh&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
