FROM critoma/amd64_u24_noble_ism_security

USER stud

WORKDIR /home/stud/app

COPY c04-rmi-server.jar /home/stud/app/

EXPOSE 1099

CMD ["/opt/software/java/jdks/jdk-21.0.2/bin/java", "-jar", "/home/stud/app/c04-rmi-server.jar", "RMI-Server-1", "1099", "rmi-server-1"]
