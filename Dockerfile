
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY backend/ backend/
COPY frontend/ frontend/

RUN mvn -B -ntp -f backend/pom.xml -DskipTests clean package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/backend/target/qldt-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar app.jar"]
