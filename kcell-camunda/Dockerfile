FROM camunda/camunda-bpm-platform:wildfly-7.6.0
RUN rm /camunda/standalone/deployments/camunda-example-invoice-7.6.0.war /camunda/standalone/deployments/camunda-h2-webapp-7.6.0.war /camunda/standalone/deployments/camunda-webapp-jboss-7.6.0.war
COPY ./camunda-webapp-jboss-7.6.0.war /camunda/standalone/deployments/camunda-webapp-jboss-7.6.0.war
RUN echo "Asia/Almaty" > /etc/timezone
