package com.game2048.data;

import com.game2048.engine.GameBoard;
import com.game2048.engine.GameState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DataManagerTest {

    @Test
    void addRecord_persistsToFileAndReloads(@TempDir Path tempDir) {
        DataManager manager = new DataManager(tempDir);

        manager.addRecord(new GameRecord(1200, 128, 55, "MANUAL"));
        manager.addRecord(new GameRecord(3400, 256, 90, "AI"));

        List<GameRecord> records = manager.loadRecords();
        assertEquals(2, records.size());
        assertEquals(3400, manager.getBestScore());
    }

    @Test
    void tileHistogram_countsOccurrencesPerMaxTile(@TempDir Path tempDir) {
        DataManager manager = new DataManager(tempDir);
        manager.addRecord(new GameRecord(100, 64, 10, "MANUAL"));
        manager.addRecord(new GameRecord(200, 64, 20, "MANUAL"));
        manager.addRecord(new GameRecord(300, 128, 30, "AI"));

        Map<Integer, Integer> histogram = manager.getTileHistogram();
        assertEquals(2, histogram.get(64));
        assertEquals(1, histogram.get(128));
    }

    @Test
    void gameState_savedAndLoadedCorrectly(@TempDir Path tempDir) {
        DataManager manager = new DataManager(tempDir);
        GameBoard board = new GameBoard();
        board.move(com.game2048.engine.Move.LEFT);

        manager.saveGameState(GameState.fromBoard(board));
        Optional<GameState> loaded = manager.loadGameState();

        assertTrue(loaded.isPresent());
        assertEquals(board.getScore(), loaded.get().getScore());
    }

    @Test
    void clearGameState_removesFile(@TempDir Path tempDir) {
        DataManager manager = new DataManager(tempDir);
        manager.saveGameState(GameState.fromBoard(new GameBoard()));
        manager.clearGameState();

        assertTrue(manager.loadGameState().isEmpty());
    }
}
