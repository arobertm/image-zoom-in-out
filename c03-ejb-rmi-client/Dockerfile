FROM critoma/amd64_u24_noble_ism_security
USER root

ENV JAVA_HOME=/opt/software/java/jdks/jdk-21.0.2
ENV PATH=$JAVA_HOME/bin:$PATH
# Copiem fișierul JAR în webapps
COPY c03-ejb-rmi-client.jar /opt/software/apache-tomee-plume-10.0.0-M3/webapps/

# Copiem configurația tomee.xml
COPY tomee.xml /opt/software/apache-tomee-plume-10.0.0-M3/conf/

# Dăm drepturi de execuție pentru scripturile din bin
RUN chmod +x /opt/software/apache-tomee-plume-10.0.0-M3/bin/*.sh

USER stud
EXPOSE 8080

CMD ["/bin/sh", "-c", "sleep 20 && /opt/software/apache-tomee-plume-10.0.0-M3/bin/catalina.sh run"]