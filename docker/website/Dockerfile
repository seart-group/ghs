FROM node:lts-alpine AS build

COPY html /html/
COPY package.json      \
     package-lock.json \
     /

RUN npm ci --silent --no-progress && npm run build

FROM nginx:1.25.4-alpine-slim
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

COPY --from=build dist /usr/share/nginx/html

ENTRYPOINT ["nginx", "-g", "daemon off;"]
