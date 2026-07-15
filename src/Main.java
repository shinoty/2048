package com.game2048;

import com.game2048.ui.GameApp;

/**
 * Отдельный launcher-класс без наследования от javafx.application.Application.
 * Это стандартный приём для сборки "fat jar": java -jar игнорирует
 * JavaFX-модульность, если main-класс сам не является Application.
 */
public class Main {
    public static void main(String[] args) {
        GameApp.main(args);
    }
}
