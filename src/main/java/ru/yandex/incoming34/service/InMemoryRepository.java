package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.structures.WeatherInfo;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InMemoryRepository {

    private final Properties properties;
    private final ConcurrentHashMap<String, WeatherInfo> cachedRepo = new ConcurrentHashMap<>();

    public Optional<JsonNode> getIfActual(String cityName) {
        WeatherInfo weatherInfo = cachedRepo.get(cityName);
        if (Objects.nonNull(weatherInfo)) {
            if (weatherInfo.getLocalDateTime().isAfter(LocalDateTime.now().minusMinutes(Integer.valueOf(properties.getProperty("retention"))))) {
                return Optional.of(weatherInfo.getJsonNode());
            } else {
                cachedRepo.remove(cityName);
            }
        }
        return Optional.empty();
    }

    public void putWeatherInfo(String cityName, WeatherInfo weatherInfo) {
        cachedRepo.put(cityName, weatherInfo);
        if (cachedRepo.size() > Integer.valueOf(properties.getProperty("cacheSize"))) {
            Pair<String, LocalDateTime> oldestRecord = null;
            for (Map.Entry<String, WeatherInfo> entry : cachedRepo.entrySet()) {
                if (Objects.isNull(oldestRecord)) {
                    oldestRecord = Pair.of(entry.getKey(), entry.getValue().getLocalDateTime());
                    continue;
                }
                if (entry.getValue().getLocalDateTime().isBefore(oldestRecord.getRight()))
                    oldestRecord = Pair.of(entry.getKey(), entry.getValue().getLocalDateTime());
            }
            cachedRepo.remove(oldestRecord.getLeft());
        }
    }

    @Scheduled(fixedDelayString = "${app.cache.retention.timeInMinutes}", timeUnit = TimeUnit.MINUTES)
    public void removeOldWeatherInfo() {
        System.out.println("=====>");
        for (Map.Entry<String, WeatherInfo> entry : cachedRepo.entrySet()) {
            if (entry.getValue().getLocalDateTime()
                    .isAfter(LocalDateTime.now().minusMinutes(Integer.valueOf(properties.getProperty("retention"))))) {
                cachedRepo.remove(entry.getKey());
            }
        }
    }

}
