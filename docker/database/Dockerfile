FROM mysql:8.0.33

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/l4lzv5k5lvm3t0kay9w71/gse.sql.gz?rlkey=x4i9ms266dhmh4yo7jkevedoe&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
