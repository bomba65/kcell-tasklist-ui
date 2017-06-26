FROM java:openjdk-8-jdk

ADD ./build/libs/asset-management.jar /opt/asset-management.jar

EXPOSE 8080
ENTRYPOINT ["bash", "-c", "sleep 10 && java -jar /opt/asset-management.jar --postgresql.host=assetsdb --postgresql.port=5432"]
