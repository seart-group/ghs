FROM node:lts-alpine AS build

COPY html /html/
COPY package.json      \
     package-lock.json \
     /

RUN npm ci --silent --no-progress && npm run build

FROM nginx:1.27.0-alpine-slim
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

ENV TZ=UTC

COPY --from=build dist /usr/share/nginx/html

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]

EXPOSE 80 8000

HEALTHCHECK \
    --start-period=10s \
    --interval=60s \
    --timeout=30s \
    --retries=5 \
    CMD nc -z 127.0.0.1 80 || exit 1
