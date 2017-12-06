FROM openjdk:8u151-jdk

ADD ./build/libs/kcell-process-app.jar /opt/kcell-process-app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "/opt/kcell-process-app.jar"]
