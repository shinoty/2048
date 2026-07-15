package com.game2048.engine;

/**
 * Неизменяемый снимок текущей (незавершённой) партии.
 * Используется исключительно для сохранения/восстановления игры между запусками
 * приложения — не путать с {@link com.game2048.data.GameRecord}, который
 * описывает уже ЗАВЕРШЁННУЮ партию для статистики.
 */
public class GameState {

    private final int[][] grid;
    private final long score;
    private final int moveCount;

    public GameState(int[][] grid, long score, int moveCount) {
        this.grid = grid;
        this.score = score;
        this.moveCount = moveCount;
    }

    public static GameState fromBoard(GameBoard board) {
        return new GameState(board.getGrid(), board.getScore(), board.getMoveCount());
    }

    public GameBoard toBoard() {
        return GameBoard.fromGrid(grid, score, moveCount);
    }

    public int[][] getGrid() {
        return grid;
    }

    public long getScore() {
        return score;
    }

    public int getMoveCount() {
        return moveCount;
    }
}
