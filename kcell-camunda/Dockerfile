FROM camunda/camunda-bpm-platform:wildfly-7.6.0
RUN rm /camunda/standalone/deployments/camunda-example-invoice-7.6.0.war /camunda/standalone/deployments/camunda-h2-webapp-7.6.0.war
COPY WEB-INF /camunda/standalone/deployments/WEB-INF
RUN cd /camunda/standalone/deployments && jar -uvf camunda-webapp-jboss-7.6.0.war WEB-INF/web.xml
