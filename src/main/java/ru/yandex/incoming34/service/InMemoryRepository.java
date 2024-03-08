package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.structures.CacheMode;
import ru.yandex.incoming34.structures.WeatherInfo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class InMemoryRepository {

    private final Properties properties;
    private final WeatherProvider weatherProvider;
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
        removeExceedingRecords();
    }

    private void removeExceedingRecords() {
        if (cachedRepo.size() > Integer.valueOf(properties.getProperty("cacheSize"))) {
            Pair<String, LocalDateTime> oldestRecord = getOldestRecord();
            WeatherInfo removedWeatherInfo = cachedRepo.remove(oldestRecord.getLeft());
            logger.log(Level.INFO, "Removed from cache out of size record: " + oldestRecord.getLeft() + " " + removedWeatherInfo.toString());
        }
    }

    private Pair<String, LocalDateTime> getOldestRecord() {
        Pair<String, LocalDateTime> oldestRecord = null;
        for (Map.Entry<String, WeatherInfo> entry : cachedRepo.entrySet()) {
            if (Objects.isNull(oldestRecord)) {
                oldestRecord = Pair.of(entry.getKey(), entry.getValue().getLocalDateTime());
                continue;
            }
            if (entry.getValue().getLocalDateTime().isBefore(oldestRecord.getRight()))
                oldestRecord = Pair.of(entry.getKey(), entry.getValue().getLocalDateTime());
        }
        return oldestRecord;
    }

    @Scheduled(initialDelayString = "${app.cache.retention.timeInMinutes}",
            fixedDelayString = "${app.cache.retention.timeInMinutes}",
            timeUnit = TimeUnit.MINUTES)
    private void refreshCache(){
        if (properties.getProperty("cacheMode").equals(CacheMode.on_demand.name())) {
            removeOldWeatherInfo();
        } else if (properties.getProperty("cacheMode").equals(CacheMode.polling.name())){
            actualizeCache();
        }
    }

    private void actualizeCache() {
        for (String cityName : cachedRepo.keySet()) {
            Pair<String, String> coordinates = cachedRepo.get(cityName).getCordinates();
            if (Objects.nonNull(coordinates)) {
               // Optional<JsonNode> node = weatherProvider.getWeatherProvider().findWeatherByCoordinates(coordinates, cityName);
            }
        }
    }


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
