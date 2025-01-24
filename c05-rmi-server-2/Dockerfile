FROM critoma/amd64_u24_noble_ism_security

USER stud

WORKDIR /home/stud/app

COPY c05-rmi-server.jar /home/stud/app/

EXPOSE 1100

CMD ["/opt/software/java/jdks/jdk-21.0.2/bin/java", "-jar", "/home/stud/app/c05-rmi-server.jar", "RMI-Server-2", "1100", "rmi-server-2"]