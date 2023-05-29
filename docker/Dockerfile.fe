FROM node:lts-alpine AS build

RUN apk update && apk add moreutils

RUN npm install uglify-js -g

COPY html html/

RUN for file in html/js/*.js; \
    do uglifyjs --compress --mangle --webkit -- $file | sponge $file; \
    done

FROM nginx:1.25.0-alpine-slim

COPY --from=build html /usr/share/nginx/html

ENTRYPOINT ["nginx", "-g", "daemon off;"]
