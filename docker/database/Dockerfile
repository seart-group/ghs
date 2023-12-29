FROM mysql:8.2.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/833d78twgzdma1hd1r19l/gse.sql.gz?rlkey=qovbf5dozvrdpt40w6gm5ezql&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
