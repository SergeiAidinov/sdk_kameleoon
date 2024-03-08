package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.exception.SdkKameleoonException;
import ru.yandex.incoming34.structures.SdkKameleoonErrors;
import ru.yandex.incoming34.structures.WeatherInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;


@AllArgsConstructor
@Service
public class OpenWeatherMapWeatherProvider implements WeatherProvider {

    private final Properties properties;
    private final InMemoryRepository inMemoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<JsonNode> requestWeather(String cityName) {
        Optional<JsonNode> weatherOptional = inMemoryRepository.getIfActual(cityName);
        if (weatherOptional.isPresent()) return weatherOptional;
        Pair<String, String> coordinatesByCityName = findCoordinatesByCityName(cityName);
        return findWeatherByCoordinates(coordinatesByCityName, cityName);
    }

    public Optional<JsonNode> findWeatherByCoordinates(Pair<String, String> coordinates, String cityName) {
        HttpURLConnection connection = prepareConnectionByCoordinates(coordinates);
        try {
            InputStream responseStream = connection.getInputStream();
            JsonNode node = objectMapper.readTree(responseStream);
            inMemoryRepository.putWeatherInfo(cityName, new WeatherInfo(LocalDateTime.now(), node, coordinates));
            return Optional.ofNullable(node);
        } catch (Exception e) {
            throw new SdkKameleoonException(SdkKameleoonErrors.WEATHER_SERVICE_UNAVAILABLE);
        } finally {
            connection.disconnect();
        }
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

    private HttpURLConnection prepareConnectionByCoordinates(Pair<String, String> coordinates) {
        String request = new StringBuilder(properties.getProperty("apiHttpWeather"))
                .append("?lat=")
                .append(coordinates.getLeft())
                .append("&lon=")
                .append(coordinates.getRight())
                .append("&appid=")
                .append(properties.getProperty("apiKey"))
                .append("&lang=")
                .append("en")
                .append("&units=")
                .append("metric")
                .toString();
        return getHttpURLConnection(request);
    }

    private HttpURLConnection getHttpURLConnection(String request) {
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

    private HttpURLConnection prepareConnectionByCityName(String cityName) {
        String request = new StringBuilder(properties.getProperty("apiHttpCoordinates"))
                .append(cityName)
                .append("&limit=1")
                .append("&appid=")
                .append(properties.getProperty("apiKey"))
                .toString();
        return getHttpURLConnection(request);
    }
}
