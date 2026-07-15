

import com.game2048.engine.GameBoard;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Визуальное представление доски: GridPane 4x4 из цветных плиток.
 * Класс не содержит игровой логики — только отрисовку по данным {@link GameBoard}.
 */
public class BoardView extends GridPane {

    private static final int CELL_SIZE = 100;
    private static final int GAP = 12;

    private static final Map<Integer, String> TILE_COLORS = new HashMap<>();
    static {
        TILE_COLORS.put(0, "#cdc1b4");
        TILE_COLORS.put(2, "#eee4da");
        TILE_COLORS.put(4, "#ede0c8");
        TILE_COLORS.put(8, "#f2b179");
        TILE_COLORS.put(16, "#f59563");
        TILE_COLORS.put(32, "#f67c5f");
        TILE_COLORS.put(64, "#f65e3b");
        TILE_COLORS.put(128, "#edcf72");
        TILE_COLORS.put(256, "#edcc61");
        TILE_COLORS.put(512, "#edc850");
        TILE_COLORS.put(1024, "#edc53f");
        TILE_COLORS.put(2048, "#edc22e");
        TILE_COLORS.put(4096, "#3c3a32");
    }

    public BoardView() {
        setHgap(GAP);
        setVgap(GAP);
        setStyle("-fx-background-color: #bbada0; -fx-padding: " + GAP + ";");
        setAlignment(Pos.CENTER);
    }

    /** Полностью перерисовывает доску по текущему состоянию. */
    public void render(GameBoard board) {
        getChildren().clear();
        int[][] grid = board.getGrid();

        for (int r = 0; r < GameBoard.SIZE; r++) {
            for (int c = 0; c < GameBoard.SIZE; c++) {
                add(createTile(grid[r][c]), c, r);
            }
        }
    }

    private StackPane createTile(int value) {
        Rectangle background = new Rectangle(CELL_SIZE, CELL_SIZE);
        background.setArcWidth(12);
        background.setArcHeight(12);
        background.setFill(Color.web(TILE_COLORS.getOrDefault(value, "#3c3a32")));

        StackPane tile = new StackPane(background);

        if (value != 0) {
            Text text = new Text(String.valueOf(value));
            text.setFont(Font.font("Arial", FontWeight.BOLD, value < 100 ? 36 : value < 1000 ? 30 : 24));
            text.setFill(value <= 4 ? Color.web("#776e65") : Color.WHITE);
            tile.getChildren().add(text);
        }
        return tile;
    }
}
