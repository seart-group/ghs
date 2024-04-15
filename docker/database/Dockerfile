FROM mysql:8.3.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /

RUN microdnf update --best \
                    --refresh \
                    --nodocs \
                    --noplugins && \
    microdnf install -y logrotate-3.14.0 && \
    microdnf clean all

# https://mariadb.com/kb/en/rotating-logs-on-unix-and-linux
COPY <<-EOF /etc/logrotate.d/mysql
/var/log/mysql/* {
        # Permissions
        su mysql mysql
        create 660 mysql mysql
        missingok
        # Size settings
        minsize 1M
        maxsize 100M
        notifempty
        # Rotate
        daily
        dateext
        dateformat .%Y_%m_%d
        maxage 180
        # Options
        copytruncate
        compress
        delaycompress
        # Scripts
        sharedscripts
        postrotate
        if test -x /usr/bin/mysqladmin && /usr/bin/mysqladmin ping &>/dev/null
                then /usr/bin/mysqladmin --defaults-file=/etc/mysql/conf.d/mysql.cnf flush-logs
        fi
        endscript
}
EOF

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/5eku4c65on1g26rnjzsx1/gse.sql.gz?rlkey=08l0sfvyj29b2wfvzw5vuuay4&dl=1" gse.sql.gz

RUN chmod 755 gse.sql.gz
