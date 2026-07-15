

import java.util.*;

/**
 * Математическое ядро игры 2048.
 * <p>
 * Отвечает исключительно за состояние доски и правила игры:
 * сдвиг, слияние тайлов, появление новых тайлов, проверку
 * окончания игры. Класс не знает ничего об UI, ИИ или хранении данных —
 * это чистая доменная логика, что позволяет тестировать её отдельно
 * и переиспользовать в AI-модуле (через {@link #copy()}).
 */
public class GameBoard {

    public static final int SIZE = 4;
    private static final Random RANDOM = new Random();

    private final int[][] grid;
    private long score;
    private int moveCount;

    /** Создаёт новую игру: пустая доска + два стартовых тайла. */
    public GameBoard() {
        this.grid = new int[SIZE][SIZE];
        this.score = 0;
        this.moveCount = 0;
        spawnTile();
        spawnTile();
    }

    /** Приватный конструктор для глубокого копирования (используется AI и copy()). */
    private GameBoard(int[][] grid, long score, int moveCount) {
        this.grid = grid;
        this.score = score;
        this.moveCount = moveCount;
    }

    // ------------------------------------------------------------------
    // Основная логика хода
    // ------------------------------------------------------------------

    /**
     * Выполняет ход в заданном направлении.
     *
     * @return true, если состояние доски изменилось (ход был возможен)
     */
    public boolean move(Move direction) {
        boolean changed = moveWithoutSpawn(direction);
        if (changed) {
            moveCount++;
            spawnTile();
        }
        return changed;
    }

    /**
     * Выполняет сдвиг/слияние БЕЗ появления нового тайла и без увеличения
     * счётчика ходов. Нужен AI-модулю: expectiminimax обязан рассматривать
     * "ход игрока" (детерминированный) и "появление тайла" (случайное,
     * CHANCE-узел) как два раздельных шага дерева поиска — иначе вероятности
     * появления 2/4 будут учтены некорректно.
     *
     * @return true, если доска изменилась (ход был допустим)
     */
    public boolean moveWithoutSpawn(Move direction) {
        int[][] before = deepCopyGrid(grid);

        // Приводим любое направление к эквивалентной задаче "сдвиг влево":
        // поворачиваем доску, применяем mergeRow построчно, поворачиваем обратно.
        switch (direction) {
            case LEFT -> applyLeft();
            case RIGHT -> {
                flipHorizontal();
                applyLeft();
                flipHorizontal();
            }
            case UP -> {
                transpose();
                applyLeft();
                transpose();
            }
            case DOWN -> {
                transpose();
                flipHorizontal();
                applyLeft();
                flipHorizontal();
                transpose();
            }
        }

        return !Arrays.deepEquals(before, grid);
    }

    /** Применяет сдвиг+слияние влево ко всем строкам доски. */
    private void applyLeft() {
        for (int row = 0; row < SIZE; row++) {
            grid[row] = mergeRow(grid[row]);
        }
    }

    /**
     * Сжимает и сливает одну строку влево.
     * Алгоритм:
     * 1. Убираем нули (компрессия).
     * 2. Проходим слева направо, сливаем соседние равные тайлы
     *    (каждый тайл участвует в слиянии не более одного раза за ход).
     * 3. Дополняем нулями справа.
     */
    private int[] mergeRow(int[] row) {
        int[] compressed = Arrays.stream(row).filter(v -> v != 0).toArray();
        List<Integer> result = new ArrayList<>(SIZE);

        int i = 0;
        while (i < compressed.length) {
            if (i + 1 < compressed.length && compressed[i] == compressed[i + 1]) {
                int mergedValue = compressed[i] * 2;
                result.add(mergedValue);
                score += mergedValue;
                i += 2; // тайл, использованный в слиянии, больше не трогаем
            } else {
                result.add(compressed[i]);
                i += 1;
            }
        }
        while (result.size() < SIZE) {
            result.add(0);
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    // ------------------------------------------------------------------
    // Вспомогательные геометрические преобразования доски
    // ------------------------------------------------------------------

    private void transpose() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = i + 1; j < SIZE; j++) {
                int tmp = grid[i][j];
                grid[i][j] = grid[j][i];
                grid[j][i] = tmp;
            }
        }
    }

    private void flipHorizontal() {
        for (int[] row : grid) {
            for (int left = 0, right = SIZE - 1; left < right; left++, right--) {
                int tmp = row[left];
                row[left] = row[right];
                row[right] = tmp;
            }
        }
    }

    // ------------------------------------------------------------------
    // Появление новых тайлов
    // ------------------------------------------------------------------

    /** Ставит новый тайл (2 с вероятностью 90%, 4 с вероятностью 10%) в случайную пустую клетку. */
    public void spawnTile() {
        List<int[]> empty = getEmptyCells();
        if (empty.isEmpty()) return;
        int[] cell = empty.get(RANDOM.nextInt(empty.size()));
        grid[cell[0]][cell[1]] = RANDOM.nextDouble() < 0.9 ? 2 : 4;
    }

    private List<int[]> getEmptyCells() {
        List<int[]> empty = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == 0) empty.add(new int[]{r, c});
            }
        }
        return empty;
    }

    // ------------------------------------------------------------------
    // Состояние игры
    // ------------------------------------------------------------------

    /** Есть ли хотя бы один допустимый ход (пустая клетка или соседи с равным значением). */
    public boolean canMove() {
        if (!getEmptyCells().isEmpty()) return true;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int v = grid[r][c];
                if (c + 1 < SIZE && grid[r][c + 1] == v) return true;
                if (r + 1 < SIZE && grid[r + 1][c] == v) return true;
            }
        }
        return false;
    }

    public boolean isGameOver() {
        return !canMove();
    }

    public int getMaxTile() {
        int max = 0;
        for (int[] row : grid) {
            for (int v : row) max = Math.max(max, v);
        }
        return max;
    }

    public long getScore() {
        return score;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public int getEmptyCellsCount() {
        return getEmptyCells().size();
    }

    /** Возвращает копию сетки (защита от внешней мутации внутреннего состояния). */
    public int[][] getGrid() {
        return deepCopyGrid(grid);
    }

    public int getTile(int row, int col) {
        return grid[row][col];
    }

    /** Глубокая копия доски — используется AI-модулем для симуляции ходов без изменения реальной игры. */
    public GameBoard copy() {
        return new GameBoard(deepCopyGrid(grid), score, moveCount);
    }

    private static int[][] deepCopyGrid(int[][] source) {
        int[][] copy = new int[SIZE][];
        for (int i = 0; i < SIZE; i++) {
            copy[i] = Arrays.copyOf(source[i], SIZE);
        }
        return copy;
    }

    /** Восстановление доски из сохранённого состояния (используется DataManager при загрузке). */
    public static GameBoard fromGrid(int[][] grid, long score, int moveCount) {
        return new GameBoard(deepCopyGrid(grid), score, moveCount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            for (int v : row) sb.append(String.format("%5d", v));
            sb.append('\n');
        }
        return sb.toString();
    }
}
