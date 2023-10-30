FROM node:lts-alpine AS build

RUN apk update --quiet
RUN apk add --no-cache \
            --quiet \
            moreutils

RUN npm install -g --silent --no-progress \
        uglify-js \
        clean-css-cli \
        html-minifier-terser

COPY html html/

RUN html-minifier-terser --collapse-whitespace \
                         --remove-comments \
                         --process-scripts='["text/html","text/x-handlebars-template"]' \
                         html/index.html \
    | sponge html/index.html

RUN for file in html/css/*.css; \
    do cleancss $file | sponge $file; \
    done

RUN for file in html/js/*.js; \
    do uglifyjs --compress --mangle --webkit -- $file | sponge $file; \
    done

FROM nginx:1.25.3-alpine-slim

COPY --from=build html /usr/share/nginx/html

ENTRYPOINT ["nginx", "-g", "daemon off;"]
