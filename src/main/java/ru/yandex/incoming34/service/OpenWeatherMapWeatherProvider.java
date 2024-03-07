package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

import static ru.yandex.incoming34.controller.SdkKameleoonControllerExceptionHandler.sdkKameleoonErrors;

@AllArgsConstructor
@Service
public class OpenWeatherMapWeatherProvider implements WeatherProvider {

    private final Properties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Cacheable("weather")
    public Optional<JsonNode> requestWeather(String cityName) {
        HttpURLConnection connection = prepareConnectionByCityName(cityName);
        InputStream responseStream;
        JsonNode node;
        try {
            responseStream = connection.getInputStream();
            node = objectMapper.readTree(responseStream);
            Pair<String, String> cordinates = Pair.of(String.valueOf(node.get(0).get("lat")), String.valueOf(node.get(0).get("lon")));
            connection = prepareConnectionByCoordinates(cordinates);
            responseStream = connection.getInputStream();
            node = objectMapper.readTree(responseStream);
        } catch (Exception exception) {
            throw new RuntimeException(sdkKameleoonErrors.get("WEATHER_SERVICE_UNAVAILABLE"));
        }
        finally {
            connection.disconnect();
        }
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
