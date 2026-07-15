package com.game2048.ui;

import com.game2048.data.DataManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Панель статистики: текущий счёт, рекорд, число сыгранных партий
 * и гистограмма максимальных достигнутых тайлов.
 */
public class StatsPanel extends VBox {

    private final Label scoreLabel = new Label();
    private final Label bestLabel = new Label();
    private final Label gamesPlayedLabel = new Label();
    private final Label histogramLabel = new Label();

    public StatsPanel() {
        setSpacing(8);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #faf8ef;");

        scoreLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        bestLabel.setStyle("-fx-font-size: 16px;");
        gamesPlayedLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #776e65;");
        histogramLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #776e65;");
        histogramLabel.setWrapText(true);

        HBox scoreRow = new HBox(20, scoreLabel, bestLabel);
        getChildren().addAll(scoreRow, gamesPlayedLabel, histogramLabel);
    }

    public void updateScore(long current) {
        scoreLabel.setText("Счёт: " + current);
    }

    /** Полное обновление панели по данным из файлового хранилища. */
    public void refreshFromStorage(DataManager dataManager) {
        bestLabel.setText("Рекорд: " + dataManager.getBestScore());
        gamesPlayedLabel.setText("Сыграно партий: " + dataManager.getGamesPlayed());

        Map<Integer, Integer> histogram = dataManager.getTileHistogram();
        StringBuilder sb = new StringBuilder("Достигнутые тайлы: ");
        histogram.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByKey().reversed())
                .forEach(e -> sb.append(e.getKey()).append("×").append(e.getValue()).append("  "));
        histogramLabel.setText(sb.toString());
    }
}
