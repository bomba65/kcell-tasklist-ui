FROM java:openjdk-8-jdk

ADD ./build/libs/kcell-process-app.jar /opt/kcell-process-app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/kcell-process-app.jar", "--postgresql.host=camundadb", "--postgresql.port=5432", "--minio.url=http://minio:9000"]
