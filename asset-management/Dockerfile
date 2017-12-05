FROM java:openjdk-8-jdk

ADD ./build/libs/asset-management.jar /opt/asset-management.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/asset-management.jar", "--spring.profiles.active=docker"]
