package com.example.agrimonitor.service;

import com.example.agrimonitor.model.Field;
import com.example.agrimonitor.model.Zone;
import com.example.agrimonitor.exception.InvalidNDVIException;

public class StressCalculator {

    // --- Публичный API ---

    /** Расчёт стресса с учётом культуры, фазы и метеофакторов (по ТЗ п.3.1 + 3.2) */
    public void calculateAndApplyStress(Zone zone, String crop, String stage,
                                        double airTemp, double rain, double humidity, double radiation) {
        try {
            validateNDVI(zone.getNdvi());
            double base = calculateStressIndex(zone.getNdvi(), crop, stage);
            double total = applyWeatherCorrection(base, airTemp, rain, humidity, radiation);
            zone.setStressIndex(total);
            zone.setStatus(getStressLevel(total));
        } catch (InvalidNDVIException e) {
            zone.setStressIndex(100);
            zone.setStatus("Ошибка NDVI");
        }
    }

    /** Базовый расчёт стресса по NDVI и оптимуму культуры (ТЗ п.3.1) */
    public double calculateStressIndex(double ndvi, String crop, String growthStage) {
        double optimalNDVI = getOptimalNDVI(crop, growthStage);
        double deviation = Math.abs(ndvi - optimalNDVI);
        double maxDeviation = 0.4;
        return Math.min(100, (deviation / maxDeviation) * 100);
    }

    /** Коррекция стресса по метеофакторам (ТЗ п.3.2) */
    public double applyWeatherCorrection(double baseStress, double temperature,
                                         double rainfall7days, double humidity, double radiation) {
        double correction = 0;

        if (temperature > 30) correction += 15;
        else if (temperature > 28) correction += 10;
        else if (temperature < 15) correction += 10;

        if (rainfall7days < 5)  correction += 20;
        else if (rainfall7days < 15) correction += 10;

        if (rainfall7days > 100) correction += 15;

        if (humidity < 40) correction += 10;

        if (radiation > 6) correction += 10;

        return Math.min(100, baseStress + correction);
    }

    /** Классификация уровня стресса (ТЗ п.3.3) */
    public String getStressLevel(double stress) {
        if (stress < 30) return "Низкий";
        if (stress < 60) return "Средний";
        return "Высокий";
    }

    /** Рекомендация по NDVI относительно оптимума культуры (ТЗ п.3.4) */
    public String getRecommendationByNDVI(double ndvi, String crop, String stage) {
        double optimal = getOptimalNDVI(crop, stage);
        if (ndvi < optimal - 0.2) {
            return "Критически низкий NDVI. Проверить полив, удобрения, болезни.";
        } else if (ndvi < optimal - 0.1) {
            return "Пониженный NDVI. Рекомендуется внесение азотных удобрений.";
        } else if (ndvi > optimal + 0.15) {
            return "Аномально высокий NDVI. Проверить на наличие сорняков или перегущенность.";
        } else {
            return "NDVI в норме. Поддерживающие мероприятия.";
        }
    }

    /** Прогнозирование стресса на N дней (ТЗ п.2.8) */
    public String forecastStress(double baseStress, double[] temps, double[] rains) {
        StringBuilder sb = new StringBuilder();
        sb.append("✈ ПРОГНОЗ СТРЕССА НА ").append(temps.length).append(" ДНЯ:\n\n");
        sb.append(String.format("Сегодня: %.0f%% (%s)%n%n", baseStress, getStressLevel(baseStress)));

        boolean willBeCritical = false;
        int criticalDay = -1;

        for (int i = 0; i < temps.length; i++) {
            double dayStress = applyWeatherCorrection(baseStress, temps[i], rains[i], 65.0, 4.0);
            String level = getStressLevel(dayStress);
            sb.append(String.format("День %d: T=%.0f°C, осадки=%.0fмм → %.0f%% (%s)%n",
                    i + 1, temps[i], rains[i], dayStress, level));
            if (dayStress >= 60 && !willBeCritical) {
                willBeCritical = true;
                criticalDay = i + 1;
            }
        }

        sb.append("\n");
        if (willBeCritical) {
            sb.append(String.format("⚠ ВНИМАНИЕ: без вмешательства через %d дн. поле войдёт в критическую зону!", criticalDay));
        } else {
            sb.append("✅ Критического уровня стресса не ожидается.");
        }
        return sb.toString();
    }

    /** Детальные рекомендации по полю (формат ТЗ п.2.5) */
    public String generateRecommendations(Field field) {
        double avgNDVI = field.getAverageNDVI();
        double avgStress = field.getAverageStress();
        String crop = field.getCrop();
        String stage = field.getGrowthPhase();
        double optimalNDVI = getOptimalNDVI(crop, stage);

        long total = field.getZones().size();
        if (total == 0) return "Нет данных по зонам.";

        long healthy  = field.getZones().stream().filter(z -> z.getNdvi() > 0.6).count();
        long moderate = field.getZones().stream().filter(z -> z.getNdvi() >= 0.4 && z.getNdvi() <= 0.6).count();
        long critical = field.getZones().stream().filter(z -> z.getNdvi() < 0.4).count();

        StringBuilder sb = new StringBuilder();
        sb.append("ПОЛЕ: ").append(field.getName())
          .append(" | ").append(crop).append(" | Фаза: ").append(stage).append("\n");
        sb.append("─────────────────────────────────────────────\n\n");

        sb.append("СВОДКА ПО ПОЛЮ:\n");
        sb.append(String.format("  Средний NDVI: %.2f (норма: %.2f)%n", avgNDVI, optimalNDVI));
        sb.append(String.format("  Здоровые зоны (NDVI >0.6): %.0f%%%n", 100.0 * healthy / total));
        sb.append(String.format("  Зоны стресса (NDVI 0.4–0.6): %.0f%%%n", 100.0 * moderate / total));
        sb.append(String.format("  Критические зоны (NDVI <0.4): %.0f%%%n", 100.0 * critical / total));
        sb.append(String.format("  Общий индекс стресса: %.0f%% (%s)%n%n",
                avgStress, getStressLevel(avgStress).toUpperCase()));

        sb.append("ВЫЯВЛЕННЫЕ ПРОБЛЕМЫ:\n");
        boolean hasProblems = false;
        if (critical > 0) {
            sb.append(String.format("  • %d зон(ы) с NDVI <0.4 → Засуха или болезни%n", critical));
            hasProblems = true;
        }
        if (moderate > 0) {
            sb.append(String.format("  • %d зон(ы) с NDVI 0.4–0.6 → Возможен дефицит азота%n", moderate));
            hasProblems = true;
        }
        if (!hasProblems) sb.append("  Серьёзных проблем не выявлено.\n");
        sb.append("\n");

        sb.append("РЕКОМЕНДАЦИИ:\n");
        if (avgStress >= 60) {
            sb.append("  Полив: Срочный полив зон с критическим стрессом (25–30 м³/га)\n");
            sb.append("  Удобрения: Внести KAS-32 или аналог по критическим зонам\n");
            sb.append("  Защита: Обработка фунгицидами при наличии симптомов болезней\n");
        } else if (avgStress >= 30) {
            sb.append("  Полив: Поддерживающий полив через 3 дня\n");
            sb.append("  Удобрения: Рассмотреть внесение азотных удобрений\n");
            sb.append("  Мониторинг: Повторная съёмка через 7 дней\n");
        } else {
            sb.append("  Растения в хорошем состоянии. Продолжать стандартный уход.\n");
            sb.append("  Плановый мониторинг через 14 дней.\n");
        }

        return sb.toString();
    }

    // --- Вспомогательные методы ---

    private void validateNDVI(double ndvi) throws InvalidNDVIException {
        if (ndvi < -1.0 || ndvi > 1.0) throw new InvalidNDVIException("NDVI = " + ndvi);
    }

    /** Таблица оптимального NDVI по культуре и фазе (ТЗ п.3.1) */
    public double getOptimalNDVI(String crop, String stage) {
        if (crop == null || stage == null) return 0.65;
        String c = crop.toLowerCase();
        String s = stage.toLowerCase();

        if (c.contains("пшениц")) {
            if (s.contains("всход"))  return 0.65;
            if (s.contains("цветен")) return 0.75;
            if (s.contains("созрев")) return 0.70;
            return 0.70; // вегетация
        }
        if (c.contains("кукуруз")) {
            if (s.contains("вегетац")) return 0.70;
            return 0.68;
        }
        if (c.contains("подсолнечник")) {
            if (s.contains("цветен")) return 0.68;
            return 0.65;
        }
        if (c.contains("соя"))    return 0.68;
        if (c.contains("томат"))  return 0.72;
        if (c.contains("огурц"))  return 0.70;
        return 0.65;
    }
}
