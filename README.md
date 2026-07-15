# Игра 2048 с ИИ — учебная ознакомительная практика 2026

**Студент:** Терещенко Платон Андреевич  
**Группа:** БИН-24-1  
**Вариант:** Б-24  
**Язык:** Java 17  

## Описание

Реализация классической игры 2048. От алгоритмического ядра (сдвиг/слияние тайлов) до ИИ на основе expectiminimax с эвристической оценкой позиции. Приложение 
демонстрирует архитектурное разделение ответственности, сохранение статистики и контейнеризацию через Docker.

## Структура репозитория

```
.
├── src/
│   ├── Main.java              # точка входа, запуск приложения
│   ├── GameBoard.java         # алгоритмическое ядро (сдвиг/слияние тайлов)
│   ├── Move.java              # enum направлений ходов (UP, DOWN, LEFT, RIGHT)
│   ├── GameState.java         # модель состояния партии для сохранения/загрузки
│   ├── AIPlayer.java          # ИИ на основе expectiminimax
│   ├── Evaluator.java         # эвристическая оценка позиции для ИИ
│   ├── DataManager.java       # работа с хранилищем (рекорды, текущая партия)
│   ├── GameRecord.java        # модель завершённой партии для статистики
│   ├── GameApp.java           # главное JavaFX-окно, оркестрация модулей
│   ├── BoardView.java         # компонент отрисовки игровой доски
│   └── StatsPanel.java        # панель со счётом, рекордом и статистикой
├── tests/
│   ├── GameBoardTest.java     # юнит-тесты алгоритма сдвига/слияния
│   └── DataManagerTest.java   # интеграционные тесты хранилища (сохранение/загрузка)
├── Dockerfile
├── docker-compose.yml
├── .gitignore
├── pom.xml
└── README.md
```

## Установка и запуск

### Локально
### Требуется JDK 17+ и Maven 3.9+.

```bash
# 1. Клонировать репозиторий
git clone https://github.com/<username>/<repo>.git
cd <repo>

# 2. Запустить через Maven (с горячей перезагрузкой)
mvn clean javafx:run

# 3. Или собрать fat-jar и запустить
mvn clean package
java -jar target/game2048-1.0.0.jar
```

### В Docker

```bash
# Собрать образ
docker build -t game2048 .

# Запустить с проксированием X11 (Linux)
xhost +local:docker
docker compose up --build

# Запустить с персистентными данными (volume)
docker run --rm -v game2048-data:/root/.game2048 \
  -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix \
  game2048
```

### Примечание для macOS/Windows:
### требуется X-сервер (XQuartz / VcXsrv).
### Альтернатива - собрать и протестировать только ядро:

```bash
docker run --rm game2048 mvn test
```

## Запуск тестов

```bash
# Все тесты (требует Maven)
mvn test

# Или через IDE (IntelliJ IDEA)
Right-click → Run All Tests
```

## Зависимости

- JDK: 17+
- Build: Maven 3.9+
- JavaFX 21.0.2 (UI)
- JUnit 5.10.2 (тестирование)
