package com.cgvsu.render_engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SimpleRenderer {
    public static void renderTest(GraphicsContext gc, int width, int height) {
        // Очистка
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);

        // Рисуем красный квадрат
        gc.setFill(Color.RED);
        gc.fillRect(100, 100, 200, 200);

        // Рисуем синие линии
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        gc.strokeLine(0, 0, width, height);
        gc.strokeLine(width, 0, 0, height);

        // Отладочный текст
        gc.setFill(Color.WHITE);
        gc.fillText("Тестовый рендер работает!", 10, 20);
        gc.fillText("Ширина: " + width + ", Высота: " + height, 10, 40);
    }
}