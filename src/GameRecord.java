package com.game2048.data;

import java.time.LocalDateTime;

/**
 * Запись об одной ЗАВЕРШЁННОЙ партии — используется для истории и статистики.
 * Хранится в records.json как элемент массива.
 */
public class GameRecord {

    private long score;
    private int maxTile;
    private int moveCount;
    private String mode;       // "MANUAL" или "AI"
    private String timestamp;  // ISO-8601, строкой — проще для ручной JSON-сериализации

    /** Пустой конструктор нужен Gson для десериализации. */
    public GameRecord() {
    }

    public GameRecord(long score, int maxTile, int moveCount, String mode) {
        this.score = score;
        this.maxTile = maxTile;
        this.moveCount = moveCount;
        this.mode = mode;
        this.timestamp = LocalDateTime.now().toString();
    }

    public long getScore() {
        return score;
    }

    public int getMaxTile() {
        return maxTile;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public String getMode() {
        return mode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] score=%d maxTile=%d moves=%d mode=%s",
                timestamp, score, maxTile, moveCount, mode);
    }
}
