FROM node:lts-alpine AS build

COPY html /html/
COPY package.json      \
     package-lock.json \
     /

RUN npm ci --silent --no-progress && npm run build

FROM nginx:1.25.4
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

ENV TZ=UTC

COPY --from=build dist /usr/share/nginx/html

ENTRYPOINT ["nginx", "-g", "daemon off;"]

HEALTHCHECK \
    --start-period=10s \
    --interval=60s \
    --timeout=30s \
    --retries=5 \
    CMD service nginx status || exit 1
