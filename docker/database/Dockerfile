FROM mysql:8.2.0

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/3nf53z4rmiir7qk27bwst/gse.sql.gz?rlkey=g9v6i9p4x9fulzpa4p8tpoy4v&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
