FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace
COPY . .
RUN ./gradlew --no-daemon :bootstrap:bootJar \
    && mkdir -p /out \
    && cp "$(find bootstrap/build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit)" /out/transaction-service.jar

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /out/transaction-service.jar app.jar
EXPOSE 8080
USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
