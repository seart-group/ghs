FROM mysql:8.0.33

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/coy1iie06xzbb3isl87xg/gse.sql.gz?rlkey=1tro7u1su3xtcpn6sc6wt0dlg&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
