FROM mysql:8.1.0

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/2i3009rbgog802cash6an/gse.sql.gz?rlkey=qxd96a7xiave5h99eqqz3gsju&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
