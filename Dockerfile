# --- build stage: compila e empacota o .jar (Maven wrapper, JDK 21) ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -B clean package -DskipTests

# --- runtime stage: apenas o JRE 21 slim + o .jar ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# A porta HTTP vem de server.port=${PORT:8080} (o Render injeta PORT).
# O profile ativo vem de SPRING_PROFILES_ACTIVE (ex.: prod) no ambiente.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
