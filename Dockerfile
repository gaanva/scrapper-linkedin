FROM selenium/standalone-firefox

WORKDIR /app
COPY ./target/lib /app/lib
COPY ./target/scrapper-facebook-0.0.1-SNAPSHOT.jar /app/
COPY ./logs /app/logs

COPY ./entrypoint.sh /app/


RUN USER=$(whoami)
USER root
RUN mkdir -p /app/drivers/binaries/linux/
RUN chmod o+rwx /app/drivers/binaries/linux/

USER $USER
RUN cp /opt/geckodriver-0.20.1 /app/drivers/binaries/linux/

RUN chmod +x ./entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]

CMD ["java", "-Xmx256m", "-classpath", "/app/lib/*:/app/scrapper-facebook-0.0.1-SNAPSHOT.jar" , "com.rocasolida.Application"]
