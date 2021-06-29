FROM openjdk:11-jre
VOLUME /tmp
ADD target/docker/crawler-1.0.0.jar crawler-1.0.0.jar
RUN bash -c 'touch /crawler-1.0.0.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar", "crawler-1.0.0.jar"]