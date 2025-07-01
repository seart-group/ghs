FROM mysql:9.3.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/yqgnrtfdasq518wr4tfpl/gse.sql.gz?rlkey=6u1gke9zwjdk26040fslg88vy&st=zm71s900&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
