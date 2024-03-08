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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
public class OpenWeatherMapMainService implements MainService {

    private final Properties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, WeatherInfo> cachedRepo = new ConcurrentHashMap<>();
    private final Logger logger = Logger.getLogger(OpenWeatherMapMainService.class.getSimpleName());

    @Override
    public Optional<JsonNode> getActualWeather(String cityName) {
        WeatherInfo weatherInfo = cachedRepo.get(cityName);
        if (Objects.isNull(weatherInfo)) return addInfoOfNewCity(cityName);
        if (properties.getProperty("app.cache.mode").equals(CacheMode.polling.name())) {
            // polling mode
            logger.log(Level.INFO, "Retrieved from cache: " + cityName + " " + weatherInfo);
            return Optional.of(weatherInfo.getJsonNode());
        } else {
            // on_demand mode
            if (weatherInfo.getLocalDateTime()
                    .isAfter(LocalDateTime.now()
                            .minusMinutes(Integer.parseInt(properties.getProperty("app.cache.retention.timeInMinutes"))))) {
                logger.log(Level.INFO, "Retrieved from cache: " + cityName + " " + weatherInfo);
                return Optional.of(weatherInfo.getJsonNode());
            } else {
                return actualizeInfoByCoordinates(weatherInfo.getCoordinates(), cityName);
            }
        }
    }

    private Optional<JsonNode> actualizeInfoByCoordinates(Pair<String, String> coordinates, String cityName) {
        Optional<JsonNode> nodeOptional = findWeatherByCoordinates(coordinates);
        logger.log(Level.INFO, "Refreshing info in cache for city of " + cityName);
        putWeatherInfo(cityName, new WeatherInfo(LocalDateTime.now(), nodeOptional.get(), coordinates));
        return nodeOptional;
    }

    private Optional<JsonNode> addInfoOfNewCity(String cityName) {
        Pair<String, String> coordinatesByCityName = findCoordinatesByCityName(cityName);
        Optional<JsonNode> node = findWeatherByCoordinates(coordinatesByCityName);
        putWeatherInfo(cityName, new WeatherInfo(LocalDateTime.now(), node.get(), coordinatesByCityName));
        return node;
    }

    public Optional<JsonNode> findWeatherByCoordinates(Pair<String, String> coordinates) {
        HttpURLConnection connection = prepareConnectionByCoordinates(coordinates);
        try {
            InputStream responseStream = connection.getInputStream();
            JsonNode node = objectMapper.readTree(responseStream);
            return Optional.ofNullable(node);
        } catch (Exception e) {
            throw new SdkKameleoonException(SdkKameleoonErrors.WEATHER_SERVICE_UNAVAILABLE);
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection prepareConnectionByCoordinates(Pair<String, String> coordinates) {
        String request = new StringBuilder(properties.getProperty("app.apiHttpWeather"))
                .append("?lat=")
                .append(coordinates.getLeft())
                .append("&lon=")
                .append(coordinates.getRight())
                .append("&appid=")
                .append(properties.getProperty("app.apiKey"))
                .append("&lang=")
                .append(properties.getProperty("app.language"))
                .append("&units=")
                .append(properties.getProperty("app.units"))
                .toString();
        return getHttpURLConnection(request);
    }

    private Pair<String, String> findCoordinatesByCityName(String cityName) {
        HttpURLConnection connection = prepareConnectionByCityName(cityName);
        try {
            InputStream responseStream = connection.getInputStream();
            JsonNode node = objectMapper.readTree(responseStream);
            if (node.isEmpty()) {
                throw new RuntimeException();
            }
            return Pair.of(String.valueOf(node.get(0).get("lat")), String.valueOf(node.get(0).get("lon")));
        } catch (RuntimeException runtimeException) {
            throw new SdkKameleoonException(SdkKameleoonErrors.CITY_NOT_FOUND, cityName);
        } catch (Exception e) {
            throw new SdkKameleoonException(SdkKameleoonErrors.WEATHER_SERVICE_UNAVAILABLE);
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection prepareConnectionByCityName(String cityName) {
        String request = new StringBuilder(properties.getProperty("app.apiHttpCoordinates"))
                .append(cityName)
                .append("&limit=1")
                .append("&appid=")
                .append(properties.getProperty("app.apiKey"))
                .toString();
        return getHttpURLConnection(request);
    }

    public void putWeatherInfo(String cityName, WeatherInfo weatherInfo) {
        cachedRepo.put(cityName, weatherInfo);
        logger.log(Level.INFO, "Put into cache: " + cityName + " " + weatherInfo);
        removeExceedingRecords();
    }

    private void removeExceedingRecords() {
        if (cachedRepo.size() > Integer.parseInt(properties.getProperty("app.cache.size"))) {
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
        if (properties.getProperty("app.cache.mode").equals(CacheMode.polling.name())) {
            logger.log(Level.INFO, "Cache actualization started: " + LocalDateTime.now());
            for (String cityName : cachedRepo.keySet()) {
                Pair<String, String> coordinates = cachedRepo.get(cityName).getCoordinates();
                if (Objects.nonNull(coordinates)) actualizeInfoByCoordinates(coordinates, cityName);
            }
            logger.log(Level.INFO, "Cache actualization finished: " + LocalDateTime.now());
        }
    }

    public HttpURLConnection getHttpURLConnection(String request) {
        HttpURLConnection connection;
        try {
            URL url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new SdkKameleoonException(SdkKameleoonErrors.WEATHER_SERVICE_UNAVAILABLE);
        }
        connection.setRequestProperty("accept", "application/json");
        return connection;
    }

}
