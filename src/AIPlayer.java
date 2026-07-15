

import com.game2048.engine.GameBoard;
import com.game2048.engine.Move;

import java.util.EnumMap;
import java.util.Map;

/**
 * ИИ-игрок на основе алгоритма expectiminimax (ожидаемый минимакс).
 * <p>
 * 2048 — игра с элементом случайности: после каждого хода игрока появляется
 * случайный тайл 2 или 4. Поэтому дерево поиска строится из узлов двух типов:
 * <ul>
 *   <li><b>MAX-узел (ход игрока)</b> — перебираем 4 направления через
 *       {@link GameBoard#moveWithoutSpawn}, выбираем ход с максимальной
 *       ожидаемой оценкой;</li>
 *   <li><b>CHANCE-узел (появление тайла)</b> — усредняем оценку по всем
 *       пустым клеткам и обоим значениям (2 с вероятностью 0.9, 4 — 0.1),
 *       взвешивая по этой вероятности.</li>
 * </ul>
 * Такое разделение важно: {@code GameBoard.move()} для обычной игры
 * объединяет сдвиг и спавн в одну операцию, а для поиска эти два шага
 * должны быть раздельными узлами дерева.
 * <p>
 * Глубина поиска адаптивная: чем меньше свободных клеток, тем меньше
 * ветвлений в CHANCE-узлах, и можно позволить себе искать глубже без
 * взрывного роста числа состояний.
 */
public class AIPlayer {

    private final Evaluator evaluator = new Evaluator();
    private final int baseDepth;

    public AIPlayer() {
        this(3);
    }

    public AIPlayer(int baseDepth) {
        this.baseDepth = baseDepth;
    }

    /** Возвращает лучший ход для текущей позиции, либо null, если допустимых ходов нет. */
    public Move getBestMove(GameBoard board) {
        Map<Move, Double> scores = evaluateAllMoves(board);
        Move best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Map.Entry<Move, Double> entry : scores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                best = entry.getKey();
            }
        }
        return best;
    }

    /** Оценка каждого из допустимых ходов — полезно и для выбора хода, и для отладки/визуализации. */
    public Map<Move, Double> evaluateAllMoves(GameBoard board) {
        int depth = adaptiveDepth(board);
        Map<Move, Double> results = new EnumMap<>(Move.class);

        for (Move move : Move.values()) {
            GameBoard next = board.copy();
            boolean changed = next.moveWithoutSpawn(move);
            if (!changed) continue; // недопустимый ход — не рассматриваем

            // После хода игрока следующий узел — CHANCE (появление тайла),
            // поэтому isPlayerTurn = false.
            double score = expectimax(next, depth - 1, false);
            results.put(move, score);
        }
        return results;
    }

    /**
     * Рекурсивная функция expectiminimax.
     *
     * @param board        доска, ДЛЯ КОТОРОЙ считается оценка (ход уже применён, спавн — ещё нет)
     * @param depth        оставшаяся глубина поиска
     * @param isPlayerTurn true — MAX-узел (ход игрока), false — CHANCE-узел (спавн тайла)
     */
    private double expectimax(GameBoard board, int depth, boolean isPlayerTurn) {
        if (depth <= 0 || board.isGameOver()) {
            return evaluator.evaluate(board);
        }
        return isPlayerTurn ? maxNode(board, depth) : chanceNode(board, depth);
    }

    /** MAX-узел: игрок выбирает ход, максимизирующий ожидаемую оценку. */
    private double maxNode(GameBoard board, int depth) {
        double best = Double.NEGATIVE_INFINITY;
        boolean anyMove = false;

        for (Move move : Move.values()) {
            GameBoard next = board.copy();
            if (!next.moveWithoutSpawn(move)) continue;
            anyMove = true;
            best = Math.max(best, expectimax(next, depth - 1, false));
        }
        return anyMove ? best : evaluator.evaluate(board);
    }

    /** CHANCE-узел: усредняем оценку по всем возможным появлениям нового тайла (2 или 4). */
    private double chanceNode(GameBoard board, int depth) {
        int[][] grid = board.getGrid();
        int emptyCount = 0;
        double total = 0;

        for (int r = 0; r < GameBoard.SIZE; r++) {
            for (int c = 0; c < GameBoard.SIZE; c++) {
                if (grid[r][c] != 0) continue;
                emptyCount++;
                total += 0.9 * expectimax(withTile(board, r, c, 2), depth - 1, true);
                total += 0.1 * expectimax(withTile(board, r, c, 4), depth - 1, true);
            }
        }
        return emptyCount == 0 ? evaluator.evaluate(board) : total / emptyCount;
    }

    private GameBoard withTile(GameBoard board, int row, int col, int value) {
        int[][] grid = board.getGrid();
        grid[row][col] = value;
        return GameBoard.fromGrid(grid, board.getScore(), board.getMoveCount());
    }

    /** Адаптивная глубина: меньше свободных клеток -> дерево уже, можно искать глубже. */
    private int adaptiveDepth(GameBoard board) {
        int empty = board.getEmptyCellsCount();
        if (empty <= 2) return baseDepth + 2;
        if (empty <= 4) return baseDepth + 1;
        return baseDepth;
    }
}
