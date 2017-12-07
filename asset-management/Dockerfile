FROM openjdk:8u151-jdk

ADD ./build/libs/asset-management.jar /opt/asset-management.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/opt/asset-management.jar"]
