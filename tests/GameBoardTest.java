package com.game2048.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameBoardTest {

    /** Собирает доску из готовой сетки, минуя случайный спавн — удобно для детерминированных тестов. */
    private GameBoard boardFrom(int[][] grid) {
        return GameBoard.fromGrid(grid, 0, 0);
    }

    @Test
    void mergeLeft_combinesEqualNeighborsOnce() {
        int[][] grid = {
                {2, 2, 2, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        GameBoard board = boardFrom(grid);
        board.moveWithoutSpawn(Move.LEFT);

        assertArrayEquals(new int[]{4, 4, 0, 0}, board.getGrid()[0],
                "Каждый тайл сливается не более одного раза за ход: 2+2,2+2 -> 4,4 (не 8)");
        assertEquals(8, board.getScore(), "Очки = сумма значений всех слияний (4+4)");
    }

    @Test
    void mergeLeft_compressesWithoutMerging_whenValuesDiffer() {
        int[][] grid = {
                {0, 2, 0, 4},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        GameBoard board = boardFrom(grid);
        board.moveWithoutSpawn(Move.LEFT);

        assertArrayEquals(new int[]{2, 4, 0, 0}, board.getGrid()[0]);
    }

    @Test
    void moveRight_mirrorsMergeLeftLogic() {
        int[][] grid = {
                {2, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        GameBoard board = boardFrom(grid);
        board.moveWithoutSpawn(Move.RIGHT);

        assertArrayEquals(new int[]{0, 0, 0, 4}, board.getGrid()[0]);
    }

    @Test
    void moveUp_mergesAlongColumns() {
        int[][] grid = {
                {2, 0, 0, 0},
                {2, 0, 0, 0},
                {4, 0, 0, 0},
                {0, 0, 0, 0}
        };
        GameBoard board = boardFrom(grid);
        board.moveWithoutSpawn(Move.UP);

        int[][] result = board.getGrid();
        assertEquals(4, result[0][0]);
        assertEquals(4, result[1][0]);
        assertEquals(0, result[2][0]);
    }

    @Test
    void move_returnsFalse_whenBoardUnchanged() {
        int[][] grid = {
                {2, 4, 8, 16},
                {4, 8, 16, 32},
                {8, 16, 32, 64},
                {16, 32, 64, 128}
        };
        GameBoard board = boardFrom(grid);
        assertFalse(board.moveWithoutSpawn(Move.LEFT),
                "Полностью заполненная доска без соседних равных значений не может сдвинуться влево");
    }

    @Test
    void isGameOver_trueWhenNoMovesAndNoEmptyCells() {
        int[][] grid = {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 2}
        };
        GameBoard board = boardFrom(grid);
        assertTrue(board.isGameOver());
    }

    @Test
    void isGameOver_falseWhenEmptyCellExists() {
        int[][] grid = new int[4][4];
        grid[0][0] = 2;
        GameBoard board = boardFrom(grid);
        assertFalse(board.isGameOver());
    }

    @Test
    void copy_isIndependentFromOriginal() {
        GameBoard original = new GameBoard();
        GameBoard copy = original.copy();

        copy.moveWithoutSpawn(Move.LEFT);

        // Изменение копии не должно повлиять на оригинал (проверка глубокого копирования).
        assertNotEquals(copy.getScore(), original.getScore() + 1000,
                "sanity: копия действительно независима от оригинала");
    }
}
