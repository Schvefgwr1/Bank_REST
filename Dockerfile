# === Stage 1: Build ===
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем pom.xml и код
COPY pom.xml .
COPY src ./src

# Собираем проект (без запуска тестов)
RUN mvn clean package -DskipTests

# === Stage 2: Runtime ===
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Копируем JAR из сборочного контейнера
COPY --from=build /app/target/*.jar app.jar

# Устанавливаем порт приложения
ENV APP_PORT=8080
ENV JAVA_OPTS=""

EXPOSE ${APP_PORT}

# Запуск приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
