FROM tiredofit/db-backup:4.0.35
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

RUN apk update --quiet && \
    apk add --no-cache \
            --quiet \
            openssh~=9.6 \
            sshpass~=1.10
