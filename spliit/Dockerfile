# https://developers.home-assistant.io/docs/add-ons/configuration#add-on-dockerfile
ARG BUILD_FROM
FROM $BUILD_FROM

# Execute during the build of the image
ARG SPLIIT_VERSION
ADD https://github.com/ChristopherJohnston/spliit2/archive/refs/tags/${SPLIIT_VERSION}.tar.gz .
RUN tar -xzf ${SPLIIT_VERSION}.tar.gz \
 && rm ${SPLIIT_VERSION}.tar.gz


RUN \
    apk update && \
    apk add --update nodejs npm

WORKDIR /spliit2-${SPLIIT_VERSION}
COPY run.sh .
RUN chmod a+x run.sh

CMD ["./run.sh"]