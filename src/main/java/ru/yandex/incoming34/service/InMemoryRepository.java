package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class InMemoryRepository {

    private final Properties properties;
    private final ConcurrentHashMap<String, WeatherInfo> cachedRepo = new ConcurrentHashMap<>();
    private final Logger logger = Logger.getLogger(InMemoryRepository.class.getSimpleName());

    public Optional<JsonNode> getIfActual(String cityName) {
        WeatherInfo weatherInfo = cachedRepo.get(cityName);
        if (Objects.nonNull(weatherInfo)) {
            if (weatherInfo.getLocalDateTime().isAfter(LocalDateTime.now().minusMinutes(Integer.valueOf(properties.getProperty("retention"))))) {
                logger.log(Level.INFO, "Retrieved from cache: " + cityName + " " + weatherInfo.toString());
                return Optional.of(weatherInfo.getJsonNode());
            } else {
                WeatherInfo removedWeatherInfo = cachedRepo.remove(cityName);
                logger.log(Level.INFO, "Removed from cache: " + cityName + " " + removedWeatherInfo.toString());
            }
        }
        return Optional.empty();
    }

    public void putWeatherInfo(String cityName, WeatherInfo weatherInfo) {
        cachedRepo.put(cityName, weatherInfo);
        logger.log(Level.INFO, "Put into cache: " + cityName + " " + weatherInfo.toString());
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
            WeatherInfo removedWeatherInfo = cachedRepo.remove(oldestRecord.getLeft());
            logger.log(Level.INFO, "Removed from cache out of size record: " + cityName + " " + removedWeatherInfo.toString());
        }
    }

    @Scheduled(initialDelayString = "${app.cache.retention.timeInMinutes}",
            fixedDelayString = "${app.cache.retention.timeInMinutes}",
            timeUnit = TimeUnit.MINUTES)
    private void removeOldWeatherInfo() {
        logger.log(Level.INFO, "Cache eviction started: " + LocalDateTime.now());
        for (Map.Entry<String, WeatherInfo> entry : cachedRepo.entrySet()) {
            if (entry.getValue().getLocalDateTime()
                    .isBefore(LocalDateTime.now().minusMinutes(Integer.valueOf(properties.getProperty("retention"))))) {
                WeatherInfo removedWeatherInfo = cachedRepo.remove(entry.getKey());
                logger.log(Level.INFO, "Removed from cache expired record: " + entry.getKey() + " " + removedWeatherInfo.toString());
            }
        }
        logger.log(Level.INFO, "Cache eviction finished: " + LocalDateTime.now());
    }

}
