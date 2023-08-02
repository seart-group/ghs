FROM mysql:8.0.33

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/hjr2dyiiw9tpwollvt0ob/gse.sql.gz?rlkey=tdzezhxpeqnaef5dwofzxj80m&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
