FROM java:openjdk-8-jdk

ADD ./build/libs/kcell-process-app.jar /opt/kcell-process-app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/kcell-process-app.jar", "--spring.profiles.active=docker"]
