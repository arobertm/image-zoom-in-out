FROM critoma/amd64_u24_noble_ism_security

USER stud

WORKDIR /home/stud/app

COPY apachetomeejms/ /home/stud/app/apachetomeejms/
COPY lib/ /home/stud/app/lib/

RUN mkdir -p out && \
    /opt/software/java/jdks/jdk-21.0.2/bin/javac -cp "lib/*" -d out apachetomeejms/JMSBrokerStart_JakartaTomEE.java

EXPOSE 61617

CMD ["/opt/software/java/jdks/jdk-21.0.2/bin/java", "-cp", "out:lib/*", "apachetomeejms.JMSBrokerStart_JakartaTomEE", "broker", "61617"]