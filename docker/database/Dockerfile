FROM mysql:8.4.0
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

WORKDIR /

RUN microdnf update --best \
                    --refresh \
                    --nodocs \
                    --noplugins && \
    microdnf install -y logrotate-3.14.0 && \
    microdnf clean all

RUN mkdir -p /var/log/mysql && \
    chown mysql:mysql /var/log/mysql && \
    chmod 750 /var/log/mysql

# https://mariadb.com/kb/en/rotating-logs-on-unix-and-linux
COPY <<-EOF /etc/logrotate.d/mysql
/var/log/mysql/*.log {
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
                then /usr/bin/mysqladmin flush-logs
        fi
        endscript
}
EOF

WORKDIR /docker-entrypoint-initdb.d

ADD "https://www.dropbox.com/scl/fi/8l8w2et56kz1w0e1o3n7e/gse.sql.gz?rlkey=pu267yxp8496s6notn1rlmy1k&st=52lox7f1&dl=1" gse.sql.gz

RUN chown mysql:mysql gse.sql.gz && \
    chmod 755 gse.sql.gz
