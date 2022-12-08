# builder
FROM gradle:4.5.1-jdk8-alpine as builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build -x test --no-daemon

# docker
FROM eclipse-temurin:8-alpine
RUN apk --no-cache add libstdc++
RUN apk --no-cache add ttf-dejavu fontconfig
RUN apk --no-cache add msttcorefonts-installer fontconfig && \
    update-ms-fonts && \
    fc-cache -f

RUN mkdir /app
COPY --from=builder /home/gradle/src/build/libs/*.jar ./app/application.jar

ENTRYPOINT ["java","-jar","/app/application.jar"]
