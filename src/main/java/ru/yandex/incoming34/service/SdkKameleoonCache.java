package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.exception.SdkKameleoonException;
import ru.yandex.incoming34.structures.CacheMode;
import ru.yandex.incoming34.structures.SdkKameleoonErrors;
import ru.yandex.incoming34.structures.WeatherInfo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.yandex.incoming34.utils.Utils.getHttpURLConnection;

@Service
@RequiredArgsConstructor
public class SdkKameleoonCache {

    private final Properties properties;
    private final WeatherProviderByCoordinate weatherProviderByCoordinate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, WeatherInfo> cachedRepo = new ConcurrentHashMap<>();
    private final Logger logger = Logger.getLogger(SdkKameleoonCache.class.getSimpleName());

    public Optional<JsonNode> getActualWeather(String cityName) {
        WeatherInfo weatherInfo = cachedRepo.get(cityName);
        if (Objects.nonNull(weatherInfo)) {
            if (weatherInfo.getLocalDateTime().isAfter(LocalDateTime.now().minusMinutes(Integer.valueOf(properties.getProperty("retention"))))) {
                logger.log(Level.INFO, "Retrieved from cache: " + cityName + " " + weatherInfo);
                return Optional.of(weatherInfo.getJsonNode());
            } else {
                return actualizeInfoForCity(cityName);
            }
        } else {
            return addInfoOfNewCity(cityName);
        }
    }

    private Optional<JsonNode> actualizeInfoForCity(String cityName) {
        WeatherInfo removedWeatherInfo = cachedRepo.remove(cityName);
        logger.log(Level.INFO, "Removed from cache: " + cityName + " " + removedWeatherInfo.toString());
        Optional<JsonNode> nodeOptional = weatherProviderByCoordinate.findWeatherByCoordinates(removedWeatherInfo.getCoordinates(), cityName);
        putWeatherInfo(cityName, new WeatherInfo(LocalDateTime.now(), nodeOptional.get(), removedWeatherInfo.getCoordinates()));
        return nodeOptional;
    }

    private Optional<JsonNode> actualizeInfoByCoordinates(Pair<String, String> coordinates, String cityName) {
        Optional<JsonNode> nodeOptional = weatherProviderByCoordinate.findWeatherByCoordinates(coordinates, cityName);
        logger.log(Level.INFO, "Refreshing info in cache for city of " + cityName);
        putWeatherInfo(cityName, new WeatherInfo(LocalDateTime.now(), nodeOptional.get(), coordinates));
        return nodeOptional;
    }

    private Optional<JsonNode> addInfoOfNewCity(String cityName) {
        Pair<String, String> coordinatesByCityName = findCoordinatesByCityName(cityName);
        Optional<JsonNode> node = weatherProviderByCoordinate.findWeatherByCoordinates(coordinatesByCityName, cityName);
        putWeatherInfo(cityName, new WeatherInfo(LocalDateTime.now(), node.get(), coordinatesByCityName));
        return node;
    }

    private Pair<String, String> findCoordinatesByCityName(String cityName) {
        HttpURLConnection connection = prepareConnectionByCityName(cityName);
        try {
            InputStream responseStream = connection.getInputStream();
            JsonNode node = objectMapper.readTree(responseStream);
            if (node.isEmpty()) {
                throw new SdkKameleoonException(SdkKameleoonErrors.CITY_NOT_FOUND, cityName);
            }
            return Pair.of(String.valueOf(node.get(0).get("lat")), String.valueOf(node.get(0).get("lon")));
        } catch (Exception e) {
            throw new SdkKameleoonException(SdkKameleoonErrors.WEATHER_SERVICE_UNAVAILABLE);
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection prepareConnectionByCityName(String cityName) {
        String request = new StringBuilder(properties.getProperty("apiHttpCoordinates"))
                .append(cityName)
                .append("&limit=1")
                .append("&appid=")
                .append(properties.getProperty("apiKey"))
                .toString();
        return getHttpURLConnection(request);
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
    private void refreshCache() {
        if (properties.getProperty("cacheMode").equals(CacheMode.on_demand.name())) {
            removeOldWeatherInfo();
        } else if (properties.getProperty("cacheMode").equals(CacheMode.polling.name())) {
            actualizeCache();
        }
    }

    private void actualizeCache() {
        logger.log(Level.INFO, "Cache actualization started: " + LocalDateTime.now());
        for (String cityName : cachedRepo.keySet()) {
            Pair<String, String> coordinates = cachedRepo.get(cityName).getCoordinates();
            if (Objects.nonNull(coordinates)) actualizeInfoByCoordinates(coordinates, cityName);
        }
        logger.log(Level.INFO, "Cache actualization finished: " + LocalDateTime.now());
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
