FROM mysql:8.3.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/mrdf1vbd5grb2nml1s99z/gse.sql.gz?rlkey=lmtpmvdlkuia0g7rlill6a0ks&st=a8s41gzx&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
