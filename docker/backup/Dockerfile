FROM databack/mysql-backup:1.0.0-rc2
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

USER root

RUN apk update --quiet && \
    apk add --no-cache \
            --quiet \
            openssh~=9.1 \
            sshpass~=1.09 \
            tzdata~=2023d

USER appuser
