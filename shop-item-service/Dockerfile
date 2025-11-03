# We need only JRE for running the artifact. No jdk required as we don't compile things on this
FROM openjdk:21-jdk-slim

ENV APPLICATION_USER ktor
RUN adduser --disabled-password --gecos '' $APPLICATION_USER || true

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

EXPOSE 8081/tcp

USER $APPLICATION_USER

# Copy any jar produced into the container as a single app.jar
COPY ./build/libs/*.jar /app/app.jar
WORKDIR /app

CMD ["java", "-jar", "app.jar"]
