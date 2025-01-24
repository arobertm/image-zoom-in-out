FROM critoma/amd64_u24_noble_ism_security

USER stud

WORKDIR /home/stud/app

COPY c01-javalin.jar /home/stud/app/
COPY lib/ /home/stud/app/lib/

EXPOSE 7000

CMD ["/opt/software/java/jdks/jdk-21.0.2/bin/java", "-jar", "/home/stud/app/c01-javalin.jar"]