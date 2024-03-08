package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.structures.WeatherInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;

import static ru.yandex.incoming34.controller.SdkKameleoonControllerExceptionHandler.sdkKameleoonErrors;

@AllArgsConstructor
@Service
public class OpenWeatherMapWeatherProvider implements WeatherProvider {

    private final Properties properties;
    private final InMemoryRepository inMemoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<JsonNode> requestWeather(String cityName) {
        Optional<JsonNode> weatherNodeOptional = inMemoryRepository.getIfActual(cityName);
        if (weatherNodeOptional.isPresent()) return Optional.of(weatherNodeOptional.get());
        HttpURLConnection connection = prepareConnectionByCityName(cityName);
        InputStream responseStream;
        JsonNode node;
        Pair<String, String> coordinates;
        try {
            responseStream = connection.getInputStream();
            node = objectMapper.readTree(responseStream);
            if (node.isEmpty()) {
                throw new RuntimeException();
            }
            coordinates = Pair.of(String.valueOf(node.get(0).get("lat")), String.valueOf(node.get(0).get("lon")));
            connection = prepareConnectionByCoordinates(coordinates);
            responseStream = connection.getInputStream();
            node = objectMapper.readTree(responseStream);
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException(sdkKameleoonErrors.get("CITY_NOT_FOUND") + cityName);
        }
        catch (Exception exception) {
            throw new RuntimeException(sdkKameleoonErrors.get("WEATHER_SERVICE_UNAVAILABLE"));
        } finally {
            connection.disconnect();
        }
        inMemoryRepository.putWeatherInfo(cityName, new WeatherInfo(LocalDateTime.now(), node, coordinates));
        return Optional.ofNullable(node);
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
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(sdkKameleoonErrors.get("WEATHER_SERVICE_UNAVAILABLE"));
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
