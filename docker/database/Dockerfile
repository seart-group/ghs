FROM mysql:8.2.0

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/wys2yqn9pnhy78psw9eqz/gse.sql.gz?rlkey=l1gcuccebrc0q4nfg5kl0m6tl&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
