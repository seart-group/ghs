FROM mysql:8.1.0

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/mm3plact32j4pkc1t52t0/gse.sql.gz?rlkey=3ygiv38wsyxmfj3vtns37ijph&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
