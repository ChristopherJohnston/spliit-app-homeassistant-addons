# https://developers.home-assistant.io/docs/add-ons/configuration#add-on-dockerfile
ARG BUILD_FROM
FROM $BUILD_FROM
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Execute during the build of the image
# ARG SPLIIT_VERSION
# RUN mkdir -p /usr/bin/spliit
# WORKDIR /usr/bin/spliit
# ADD https://github.com/ChristopherJohnston/spliit2/archive/refs/tags/${SPLIIT_VERSION}.tar.gz .
# RUN tar -xvzf ${SPLIIT_VERSION}.tar.gz \
  # && rm ${SPLIIT_VERSION}.tar.gz

RUN \
    apk update \
    && apk add --no-cache --update nodejs npm nginx \
    && apk add --no-cache --virtual .build-dependencies git \
    \
    && git clone --depth=1 \
      https://github.com/spliit-app/spliit /temp/spliit

WORKDIR /temp/spliit
COPY .env .
COPY next.config.js .

RUN npm install --ignore-scripts \
        && npm install sharp \
        && npm install -g prisma \
        && prisma generate \
        && npm run build \
        && ls -la \
        && mkdir -p /var/www/spliit \
        && cd /var/www/spliit \
        && cp /temp/spliit/next.config.js ./ \
        && cp -r /temp/spliit/public ./ \
        && cp -r /temp/spliit/build ./ \
        && cp -r /temp/spliit/node_modules ./ \
        && cp -r /temp/spliit/prisma ./ \
        && cp /temp/spliit/package.json ./ \
        \
        && apk del --no-cache --purge .build-dependencies \
        && rm -f -r \
          /etc/nginx \
          /tmp/* \
          /temp/* \
        && mkdir -p /var/lib/nginx/tmp /var/log/nginx /var/lib/nginx/logs /var/lib/nginx/tmp/client_body \
        && chown -R nginx:nginx /var/lib/nginx \
        && chmod -R 777 /var/lib/nginx /var/log/nginx /var/lib/nginx/logs /var/lib/nginx/tmp/client_body


COPY rootfs /