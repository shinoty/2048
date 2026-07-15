package com.game2048.ui;

import com.game2048.ai.AIPlayer;
import com.game2048.data.DataManager;
import com.game2048.data.GameRecord;
import com.game2048.engine.GameBoard;
import com.game2048.engine.Move;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Точка входа UI-слоя. Отвечает за связывание всех модулей:
 * {@link GameBoard} (движок), {@link AIPlayer} (ИИ), {@link DataManager}
 * (файловое хранилище) и визуальные компоненты {@link BoardView}/{@link StatsPanel}.
 * Сама логика игры и ИИ здесь не реализуется — только оркестрация.
 */
public class GameApp extends Application {

    private static final Duration AI_MOVE_INTERVAL = Duration.millis(150);

    private GameBoard board;
    private final AIPlayer aiPlayer = new AIPlayer(3);
    private final DataManager dataManager = new DataManager();

    private BoardView boardView;
    private StatsPanel statsPanel;
    private Timeline aiTimeline;
    private boolean aiMode = false;

    @Override
    public void start(Stage stage) {
        board = dataManager.loadGameState()
                .map(state -> GameBoard.fromGrid(state.getGrid(), state.getScore(), state.getMoveCount()))
                .orElseGet(GameBoard::new);

        boardView = new BoardView();
        statsPanel = new StatsPanel();

        Button newGameBtn = new Button("Новая игра");
        newGameBtn.setOnAction(e -> startNewGame());

        ToggleButton aiToggle = new ToggleButton("Автоигра (ИИ)");
        aiToggle.setOnAction(e -> toggleAiMode(aiToggle.isSelected()));

        HBox controls = new HBox(10, newGameBtn, aiToggle);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(boardView);
        root.setBottom(statsPanel);
        root.setStyle("-fx-background-color: #faf8ef;");

        Scene scene = new Scene(root, 480, 620);
        scene.setOnKeyPressed(this::handleKeyPress);

        stage.setTitle("2048");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        refreshUI();

        stage.setOnCloseRequest(e -> onExit());
    }

    private void handleKeyPress(javafx.scene.input.KeyEvent event) {
        if (aiMode || board.isGameOver()) return;

        Move move = switch (event.getCode()) {
            case UP, W -> Move.UP;
            case DOWN, S -> Move.DOWN;
            case LEFT, A -> Move.LEFT;
            case RIGHT, D -> Move.RIGHT;
            default -> null;
        };
        if (move != null) {
            applyMove(move, "MANUAL");
        }
    }

    private void toggleAiMode(boolean enabled) {
        aiMode = enabled;
        if (enabled) {
            aiTimeline = new Timeline(new KeyFrame(AI_MOVE_INTERVAL, e -> performAiMove()));
            aiTimeline.setCycleCount(Timeline.INDEFINITE);
            aiTimeline.play();
        } else if (aiTimeline != null) {
            aiTimeline.stop();
        }
    }

    private void performAiMove() {
        if (board.isGameOver()) {
            aiTimeline.stop();
            aiMode = false;
            return;
        }
        Move best = aiPlayer.getBestMove(board);
        if (best == null) {
            aiTimeline.stop();
            aiMode = false;
            return;
        }
        applyMove(best, "AI");
    }

    /** Единая точка применения хода: обновляет доску, UI, сохраняет прогресс, проверяет конец игры. */
    private void applyMove(Move move, String mode) {
        boolean changed = board.move(move);
        if (!changed) return;

        refreshUI();
        dataManager.saveGameState(com.game2048.engine.GameState.fromBoard(board));

        if (board.isGameOver()) {
            finishGame(mode);
        }
    }

    private void finishGame(String mode) {
        GameRecord record = new GameRecord(board.getScore(), board.getMaxTile(), board.getMoveCount(), mode);
        dataManager.addRecord(record);
        dataManager.clearGameState();
        statsPanel.refreshFromStorage(dataManager);

        if (aiTimeline != null) aiTimeline.stop();
        aiMode = false;
    }

    private void startNewGame() {
        if (aiTimeline != null) aiTimeline.stop();
        aiMode = false;
        dataManager.clearGameState();
        board = new GameBoard();
        refreshUI();
    }

    private void refreshUI() {
        boardView.render(board);
        statsPanel.updateScore(board.getScore());
        statsPanel.refreshFromStorage(dataManager);
    }

    private void onExit() {
        if (!board.isGameOver()) {
            dataManager.saveGameState(com.game2048.engine.GameState.fromBoard(board));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
