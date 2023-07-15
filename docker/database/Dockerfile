FROM mysql:8.0.33

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/ncq090zg1h6553ib9hxij/gse.sql.gz?rlkey=im7bgcwfs0jg6jkywuegahxcj&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
