

import com.game2048.engine.GameState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Файловое хранилище данных приложения. По требованиям проекта данные
 * хранятся в виде файлов (без БД/СУБД) — используется формат JSON,
 * который проще версионировать/дебажить и переносить между машинами
 * (что удобно и в контексте Docker-контейнера с volume-маунтом).
 * <p>
 * Каталог данных: {@code ~/.game2048/}
 * <ul>
 *   <li>{@code records.json} — история всех завершённых партий;</li>
 *   <li>{@code current_game.json} — незавершённая партия (для восстановления
 *       при следующем запуске).</li>
 * </ul>
 */
public class DataManager {

    private final Path dataDir;
    private final Path recordsFile;
    private final Path currentGameFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public DataManager() {
        this(Paths.get(System.getProperty("user.home"), ".game2048"));
    }

    /** Конструктор с явным путём — удобно для тестов и для запуска в Docker с volume. */
    public DataManager(Path dataDir) {
        this.dataDir = dataDir;
        this.recordsFile = dataDir.resolve("records.json");
        this.currentGameFile = dataDir.resolve("current_game.json");
        ensureDataDirExists();
    }

    private void ensureDataDirExists() {
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new DataStorageException("Не удалось создать каталог данных: " + dataDir, e);
        }
    }

    // ------------------------------------------------------------------
    // Рекорды завершённых партий
    // ------------------------------------------------------------------

    public synchronized List<GameRecord> loadRecords() {
        if (!Files.exists(recordsFile)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(recordsFile)) {
            Type listType = new TypeToken<ArrayList<GameRecord>>() {}.getType();
            List<GameRecord> records = gson.fromJson(reader, listType);
            return records != null ? records : new ArrayList<>();
        } catch (IOException e) {
            throw new DataStorageException("Не удалось прочитать " + recordsFile, e);
        }
    }

    /** Добавляет запись о завершённой партии и сразу сохраняет файл на диск. */
    public synchronized void addRecord(GameRecord record) {
        List<GameRecord> records = loadRecords();
        records.add(record);
        saveRecords(records);
    }

    private void saveRecords(List<GameRecord> records) {
        try (Writer writer = Files.newBufferedWriter(recordsFile)) {
            gson.toJson(records, writer);
        } catch (IOException e) {
            throw new DataStorageException("Не удалось сохранить " + recordsFile, e);
        }
    }

    // ------------------------------------------------------------------
    // Статистика (вычисляется поверх records.json — отдельный файл не нужен)
    // ------------------------------------------------------------------

    public long getBestScore() {
        return loadRecords().stream()
                .mapToLong(GameRecord::getScore)
                .max()
                .orElse(0);
    }

    /** Гистограмма достигнутых максимальных тайлов: значение тайла -> сколько раз он был лучшим в партии. */
    public Map<Integer, Integer> getTileHistogram() {
        Map<Integer, Integer> histogram = new TreeMap<>();
        for (GameRecord record : loadRecords()) {
            histogram.merge(record.getMaxTile(), 1, Integer::sum);
        }
        return histogram;
    }

    public int getGamesPlayed() {
        return loadRecords().size();
    }

    // ------------------------------------------------------------------
    // Сохранение/восстановление незавершённой партии
    // ------------------------------------------------------------------

    public synchronized void saveGameState(GameState state) {
        try (Writer writer = Files.newBufferedWriter(currentGameFile)) {
            gson.toJson(state, writer);
        } catch (IOException e) {
            throw new DataStorageException("Не удалось сохранить текущую партию", e);
        }
    }

    public synchronized Optional<GameState> loadGameState() {
        if (!Files.exists(currentGameFile)) {
            return Optional.empty();
        }
        try (Reader reader = Files.newBufferedReader(currentGameFile)) {
            GameState state = gson.fromJson(reader, GameState.class);
            return Optional.ofNullable(state);
        } catch (IOException e) {
            throw new DataStorageException("Не удалось загрузить текущую партию", e);
        }
    }

    public synchronized void clearGameState() {
        try {
            Files.deleteIfExists(currentGameFile);
        } catch (IOException e) {
            throw new DataStorageException("Не удалось удалить файл текущей партии", e);
        }
    }

    /** Непроверяемое исключение уровня хранилища — оборачивает IOException для упрощения вызовов. */
    public static class DataStorageException extends RuntimeException {
        public DataStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
