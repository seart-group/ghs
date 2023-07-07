FROM mysql:8.0.33

WORKDIR /docker-entrypoint-initdb.d

RUN curl -L -o gse.sql.gz \
    https://www.dropbox.com/scl/fi/go9ok4gbn571abz0eazns/gse.sql.gz?rlkey=g4b8bjy8geohmuvp5d621rxks&dl=1
