FROM mysql:8.0.33

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/dlga9rpv4je5cyxcrzh0u/gse.sql.gz?rlkey=9lroivo683pcfpffrtprtnx1v&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
