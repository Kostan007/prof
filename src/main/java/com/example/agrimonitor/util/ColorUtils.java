package com.example.agrimonitor.util;

import javafx.scene.paint.Color;

/**
 * Утилита для цветов тепловой карты
 */
public class ColorUtils {

    /**
     * Возвращает цвет по уровню стресса (0-100)
     */
    public static Color getStressColor(double stressIndex) {
        // Ограничиваем диапазон
        stressIndex = Math.max(0, Math.min(100, stressIndex));

        if (stressIndex <= 30) {
            // Зелёный
            double intensity = 0.4 + (30 - stressIndex) / 50.0;
            return Color.color(0.1, Math.min(1.0, intensity), 0.1);
        }
        else if (stressIndex <= 60) {
            // Жёлто-оранжевый
            double ratio = (stressIndex - 30) / 30.0;
            double red = 0.9 * ratio;
            double green = 0.8 - 0.6 * ratio;
            return Color.color(red, green, 0.0);
        }
        else {
            // Красный
            double ratio = (stressIndex - 60) / 40.0;
            double red = 0.95 + 0.05 * ratio;
            double green = 0.3 - 0.3 * ratio;
            return Color.color(red, Math.max(0.0, green), 0.0);
        }
    }

    /**
     * Простая версия (если хочешь использовать)
     */
    public static Color getSimpleStressColor(double stressIndex) {
        stressIndex = Math.max(0, Math.min(100, stressIndex));
        if (stressIndex < 30) return Color.GREEN;
        if (stressIndex < 60) return Color.ORANGE;
        return Color.RED;
    }
}