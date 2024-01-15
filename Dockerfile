FROM openjdk:17-alpine
ARG APP_PORT=8765
ARG VERSION=0.0.1
WORKDIR /app
COPY ./application/build/libs/application-${VERSION}.jar /app/app.jar
EXPOSE ${APP_PORT}
CMD ["java", "-jar", "app.jar"]