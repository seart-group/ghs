FROM mysql:8.1.0

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/6zv941qhjea1jsnlihi4k/gse.sql.gz?rlkey=pajy2ybcinqgmos1vgzhw2dhc&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
