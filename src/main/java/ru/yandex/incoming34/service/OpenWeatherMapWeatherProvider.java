package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.structures.Coordinates;
import ru.yandex.incoming34.structures.UserRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

import static ru.yandex.incoming34.controller.SdkKameleoonControllerExceptionHandler.sdkKameleoonErrors;

@AllArgsConstructor
@Service
public class OpenWeatherMapWeatherProvider /*implements WeatherProvider*/ {

    private final Properties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<JsonNode> requestWheather(UserRequest userRequest) {
        HttpURLConnection connection = prepareConnection(userRequest);
        InputStream responseStream;
        JsonNode weatherNode;
        try {
            responseStream = connection.getInputStream();
            weatherNode = objectMapper.readTree(responseStream);
        } catch (Exception exception) {
            throw new RuntimeException(sdkKameleoonErrors.get("WEATHER_SERVICE_UNAVAILABLE"));
        } finally {
            connection.disconnect();
        }
        return Optional.ofNullable(weatherNode);
    }

    private HttpURLConnection prepareConnection(UserRequest userRequest) {
        String request = new StringBuilder(properties.getProperty("apiHttp"))
                .append("?")
                .append("lat=")
                .append(userRequest.getCoordinates().getLatitude())
                .append("&lon=")
                .append(userRequest.getCoordinates().getLongitude())
                .append("&appid=")
                .append(properties.getProperty("appId"))
                .append("&lang=")
                .append(userRequest.getLng().name())
                .append("&units=")
                .append(userRequest.getMetrics().name())
                .toString();
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
}
