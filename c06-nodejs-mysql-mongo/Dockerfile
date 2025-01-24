FROM critoma/amd64_u24_noble_ism_security

USER root

RUN echo "stud ALL=(ALL:ALL) NOPASSWD: ALL" > /etc/sudoers.d/stud

USER stud
WORKDIR /home/stud/app

COPY --chown=stud:stud . .

ENV PATH="/opt/software/node-v22.10.0-linux-x64/bin:${PATH}"

EXPOSE 3000 27017 3306

COPY --chown=stud:stud start.sh /home/stud/start.sh
RUN chmod +x /home/stud/start.sh

CMD ["/home/stud/start.sh"]