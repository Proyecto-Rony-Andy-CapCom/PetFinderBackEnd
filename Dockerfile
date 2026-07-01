# --- Etapa 1: Construcción (Builder) ---
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiamos los archivos de configuración y el código fuente
COPY pom.xml .
COPY src ./src

# Compilamos el proyecto (saltando las pruebas para que sea más rápido)
RUN mvn clean package -DskipTests

# --- Etapa 2: Producción (Runtime) ---
# Usamos una imagen Alpine, que es extremadamente ligera y segura
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos ÚNICAMENTE el archivo .jar generado en la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponemos el puerto estándar de Spring Boot
EXPOSE 8080

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]