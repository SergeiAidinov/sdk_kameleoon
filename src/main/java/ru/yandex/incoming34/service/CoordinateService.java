package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

import static ru.yandex.incoming34.utils.Utils.getHttpURLConnection;

@Service
@AllArgsConstructor
@Getter
public class CoordinateService {

    private final Properties properties;
    //private final InMemoryRepository inMemoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<JsonNode> findWeatherByCoordinates(Pair<String, String> coordinates, String cityName) {
        HttpURLConnection connection = prepareConnectionByCoordinates(coordinates);
        try {
            InputStream responseStream = connection.getInputStream();
            JsonNode node = objectMapper.readTree(responseStream);
            //inMemoryRepository.putWeatherInfo(cityName, new WeatherInfo(LocalDateTime.now(), node, coordinates));
            return Optional.ofNullable(node);
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

    /*private HttpURLConnection getHttpURLConnection(String request) {
        HttpURLConnection connection;
        try {
            URL url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new SdkKameleoonException(SdkKameleoonErrors.WEATHER_SERVICE_UNAVAILABLE);
        }
        connection.setRequestProperty("accept", "application/json");
        return connection;
    }*/
}
