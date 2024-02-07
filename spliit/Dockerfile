# https://developers.home-assistant.io/docs/add-ons/configuration#add-on-dockerfile
ARG BUILD_FROM
FROM $BUILD_FROM

# Execute during the build of the image
ARG SPLIIT_VERSION
RUN mkdir -p /usr/bin/spliit
WORKDIR /usr/bin/spliit
ADD https://github.com/ChristopherJohnston/spliit2/archive/refs/tags/${SPLIIT_VERSION}.tar.gz .
RUN tar -xvzf ${SPLIIT_VERSION}.tar.gz \
  && rm ${SPLIIT_VERSION}.tar.gz

RUN \
    apk update && \
    apk add --update nodejs npm

WORKDIR /usr/bin/spliit/spliit2-${SPLIIT_VERSION}

COPY .env .
COPY next.config.js .
COPY run.sh .
COPY server.js .
RUN chmod a+x run.sh
RUN chmod a+x server.js

RUN npm install --ignore-scripts \
        && npm install sharp \
        && npm install -g prisma \
        && prisma generate \
        && npm run build

CMD ["./run.sh"]