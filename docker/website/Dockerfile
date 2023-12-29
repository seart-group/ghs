FROM node:lts-alpine AS build

RUN apk update --quiet && \
    apk add --no-cache \
            --quiet \
            moreutils~=0.68

RUN npm install -g --silent --no-progress \
        uglify-js@3.17.4 \
        clean-css-cli@5.6.3 \
        html-minifier-terser@7.2.0

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
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

COPY --from=build html /usr/share/nginx/html

ENTRYPOINT ["nginx", "-g", "daemon off;"]
