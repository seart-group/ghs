FROM mysql:8.3.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/pfyq5ukfh68arxq94kxtk/gse.sql.gz?rlkey=1p6nt39rrce6883fnqt0nranq&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
