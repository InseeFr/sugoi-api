FROM tomcat:9.0.38-jdk11-openjdk
EXPOSE 8080 
EXPOSE 8443

#Add RemoteIpValve
RUN sed -i 's|\
  </Host>|\
    <Valve className="org.apache.catalina.valves.RemoteIpValve" \
      remoteIpHeader="X-Forwarded-For" \
      protocolHeader="X-Forwarded-Proto"/>\
  </Host>|' \
  /usr/local/tomcat/conf/server.xml


COPY sugoi-api-services/target/sugoi-api.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]