# ---------- Стадия 1: сборка (Maven + JDK) ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Сначала копируем только pom.xml — так Docker кэширует слой с зависимостями
# и не перекачивает их при каждом изменении исходников.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Стадия 2: runtime (тонкий образ только с JRE) ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Fat-jar со всеми зависимостями (JavaFX, Gson), собранный maven-shade-plugin.
COPY --from=build /app/target/game2048-1.0.0.jar app.jar

# Каталог для файлового хранилища (records.json, current_game.json) —
# монтируется как volume, чтобы данные переживали пересоздание контейнера.
VOLUME ["/root/.game2048"]

# JavaFX — GUI-приложение: контейнеру нужен доступ к X11 хоста.
# Запуск на Linux-хосте:
#   xhost +local:docker
#   docker run --rm -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix \
#       -v game2048-data:/root/.game2048 game2048
ENTRYPOINT ["java", "-jar", "app.jar"]
