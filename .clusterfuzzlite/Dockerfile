FROM gcr.io/oss-fuzz-base/base-builder-jvm:v1
RUN apt-get update && apt-get install -y make autoconf automake libtool
COPY . $SRC/java-sdk
WORKDIR java-sdk
COPY .clusterfuzzlite/build.sh $SRC/
