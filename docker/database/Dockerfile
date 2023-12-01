FROM mysql:8.2.0

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/ftmpx1d0kfd7pmka9a3ma/gse.sql.gz?rlkey=u7fcjsuetr1ibibib3s3tjn6n&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
