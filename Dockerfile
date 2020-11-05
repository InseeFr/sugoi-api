FROM tomcat:9.0.38-jdk11-openjdk
EXPOSE 8080 
EXPOSE 8443

COPY sugoi-api-services/target/sugoi-api.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]