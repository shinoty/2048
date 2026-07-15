package com.game2048.ai;

import com.game2048.engine.GameBoard;

/**
 * Эвристическая функция оценки позиции для алгоритма expectiminimax.
 * <p>
 * Комбинирует три классических для 2048-ботов эвристики:
 * <ul>
 *   <li><b>Позиционный вес (snake pattern)</b> — поощряет удержание крупных
 *       тайлов в углу и построение "змейки" по убыванию, что снижает
 *       фрагментацию поля;</li>
 *   <li><b>Число свободных клеток</b> — больше свободного места = больше
 *       возможностей для манёвра и меньше риск блокировки;</li>
 *   <li><b>Гладкость (smoothness)</b> — штраф за большие перепады значений
 *       между соседними тайлами, так как их сложнее слить.</li>
 * </ul>
 */
public class Evaluator {

    // Весовая матрица типа "змейка": максимальный вес в углу (0,0),
    // значения убывают змейкой, подталкивая крупные тайлы к одному краю.
    private static final double[][] WEIGHTS = {
            {65536, 32768, 16384, 8192},
            {512,   1024,  2048,  4096},
            {256,   128,   64,    32},
            {1,     2,     4,     8}
    };

    private static final double EMPTY_CELL_WEIGHT = 270.0;
    private static final double SMOOTHNESS_WEIGHT = 0.1;
    private static final double POSITIONAL_WEIGHT = 1.0;

    public double evaluate(GameBoard board) {
        int[][] grid = board.getGrid();

        double positionalScore = positionalScore(grid);
        double emptyScore = board.getEmptyCellsCount() * EMPTY_CELL_WEIGHT;
        double smoothnessScore = smoothness(grid) * SMOOTHNESS_WEIGHT;

        return POSITIONAL_WEIGHT * positionalScore + emptyScore + smoothnessScore;
    }

    private double positionalScore(int[][] grid) {
        double sum = 0;
        for (int r = 0; r < GameBoard.SIZE; r++) {
            for (int c = 0; c < GameBoard.SIZE; c++) {
                sum += grid[r][c] * WEIGHTS[r][c];
            }
        }
        return sum;
    }

    /**
     * Штраф за перепады значений между соседними тайлами (в лог-шкале,
     * так как значения тайлов растут степенями двойки).
     * Возвращает отрицательное число: чем больше перепад — тем ниже оценка.
     */
    private double smoothness(int[][] grid) {
        double penalty = 0;
        for (int r = 0; r < GameBoard.SIZE; r++) {
            for (int c = 0; c < GameBoard.SIZE; c++) {
                if (grid[r][c] == 0) continue;
                double value = Math.log(grid[r][c]) / Math.log(2);

                if (c + 1 < GameBoard.SIZE && grid[r][c + 1] != 0) {
                    double neighbor = Math.log(grid[r][c + 1]) / Math.log(2);
                    penalty -= Math.abs(value - neighbor);
                }
                if (r + 1 < GameBoard.SIZE && grid[r + 1][c] != 0) {
                    double neighbor = Math.log(grid[r + 1][c]) / Math.log(2);
                    penalty -= Math.abs(value - neighbor);
                }
            }
        }
        return penalty;
    }
}
