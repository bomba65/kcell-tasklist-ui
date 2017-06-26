FROM java:openjdk-8-jdk

ADD ./build/libs/kcell-process-app.jar /opt/kcell-process-app.jar

EXPOSE 8080
ENTRYPOINT ["bash", "-c", "sleep 10 && java -jar /opt/kcell-process-app.jar --postgresql.host=camundadb --postgresql.port=5432"]
