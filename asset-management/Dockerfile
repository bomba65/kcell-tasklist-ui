# builder
FROM gradle:4.2.1-jdk8-alpine as builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build -x test --no-daemon

# docker
FROM eclipse-temurin:8-alpine

RUN mkdir /app
COPY --from=builder /home/gradle/src/build/libs/*.jar ./app/application.jar

ENTRYPOINT ["java","-jar","/app/application.jar"]
