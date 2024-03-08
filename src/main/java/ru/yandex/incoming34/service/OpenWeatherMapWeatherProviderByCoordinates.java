package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.exception.SdkKameleoonException;
import ru.yandex.incoming34.structures.SdkKameleoonErrors;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.Properties;

import static ru.yandex.incoming34.utils.Utils.getHttpURLConnection;

@Service
@AllArgsConstructor
@Getter
public class OpenWeatherMapWeatherProviderByCoordinates implements WeatherProviderByCoordinates {

    private final Properties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<JsonNode> findWeatherByCoordinates(Pair<String, String> coordinates, String cityName) {
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
}
