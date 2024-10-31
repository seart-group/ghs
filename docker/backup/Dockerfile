FROM tiredofit/db-backup:4.1.8
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

RUN apk update --quiet && \
    apk add --no-cache \
            --quiet \
            openssh~=9.7 \
            sshpass~=1.10
