package com.example.agrimonitor.service;

import com.example.agrimonitor.model.Field;
import com.example.agrimonitor.model.Zone;
import com.example.agrimonitor.exception.InvalidNDVIException;

public class StressCalculator {

    /**
     * Основной расчёт индекса стресса для зоны
     */
    public void calculateStress(Zone zone) {
        try {
            validateNDVI(zone.getNdvi());

            // Базовый стресс от NDVI (чем ниже NDVI — тем выше стресс)
            double baseStress = (1.0 - zone.getNdvi()) * 100;

            // Коррекция по температуре почвы
            double tempStress = Math.abs(zone.getSoilTemp() - 22.0) * 2.5;

            // Коррекция по влажности почвы
            double moistureStress = (50.0 - zone.getSoilMoisture()) * 1.2;

            // Итоговый стресс
            double totalStress = baseStress + tempStress + moistureStress;

            // Ограничиваем диапазон 0-100
            totalStress = Math.max(0, Math.min(100, totalStress));

            zone.setStressIndex(totalStress);
            zone.setStatus(getStressStatus(totalStress));

        } catch (InvalidNDVIException e) {
            zone.setStressIndex(100);
            zone.setStatus("Ошибка NDVI");
        }
    }

    private void validateNDVI(double ndvi) throws InvalidNDVIException {
        if (ndvi < -1.0 || ndvi > 1.0) {
            throw new InvalidNDVIException("NDVI = " + ndvi);
        }
    }

    private String getStressStatus(double stress) {
        if (stress < 30) return "Здорово";
        else if (stress < 60) return "Умеренный стресс";
        else return "Критический стресс";
    }

    /**
     * Генерация рекомендаций по текущему состоянию поля
     */
    public String generateRecommendations(Field field) {
        double avgStress = field.getAverageStress();
        StringBuilder sb = new StringBuilder();

        sb.append("Общий индекс стресса: ").append(String.format("%.1f", avgStress)).append("%\n\n");

        if (avgStress < 25) {
            sb.append("✅ Растения в хорошем состоянии.\n");
            sb.append("Рекомендация: Продолжать стандартный уход.");
        }
        else if (avgStress < 50) {
            sb.append("⚠️ Умеренный стресс по полю.\n");
            sb.append("Рекомендации:\n");
            sb.append("• Проверить влажность почвы\n");
            sb.append("• Рассмотреть прикорневую подкормку\n");
            sb.append("• Провести осмотр на наличие вредителей");
        }
        else {
            sb.append("🚨 Критический стресс!\n");
            sb.append("Необходимы срочные меры:\n");
            sb.append("• Срочный полив (если влажность низкая)\n");
            sb.append("• Подкормка азотом/комплексным удобрением\n");
            sb.append("• Обработка фунгицидами (при подозрении на болезни)");
        }

        return sb.toString();
    }
}