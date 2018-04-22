FROM selenium/standalone-firefox

WORKDIR /app

COPY ./target/scrapper-facebook-0.0.1-SNAPSHOT.jar /app/
COPY ./drivers/binaries/linux /app/drivers/binaries/linux
COPY ./target/lib /app/lib
COPY ./logs /app/logs


CMD ["java", "-Xmx256m", "-classpath", "/app/lib/*:/app/scrapper-facebook-0.0.1-SNAPSHOT.jar" , "com.rocasolida.Application"]
