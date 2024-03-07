package ru.yandex.incoming34.test_task_for_employment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import ru.yandex.incoming34.test_task_for_employment.structures.Coordinates;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static ru.yandex.incoming34.test_task_for_employment.utils.Utils.findNode;
@AllArgsConstructor
public class OpenWeatherMapTemperatureProvider implements TemperatureProvider{

    private List<String> nodeList;
    private final Properties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<String> requestTemperature(Coordinates coordinates) {
        HttpURLConnection connection = prepareConnection(coordinates);
        InputStream responseStream;
        JsonNode root;
        try {
            responseStream = connection.getInputStream();
            root = objectMapper.readTree(responseStream);
        } catch (Exception exception) {
            throw new RuntimeException("Не удалось получить информацию о погоде");
        } finally {
            connection.disconnect();
        }
        return findNode(root, new ArrayList<>(nodeList));
    }

    private HttpURLConnection prepareConnection(Coordinates coordinates) {
        String request = new StringBuilder(properties.getProperty("apiHttp"))
                .append("?")
                .append("lat=")
                .append(coordinates.getLatitude())
                .append("&lon=")
                .append(coordinates.getLongitude())
                .append("&appid=")
                .append(properties.getProperty("appId"))
                .append("&lang=ru&")
                .append("units=metric")
                .toString();
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось подключиться к серверу - поставщику сведений о температуре");
        }
        connection.setRequestProperty("accept", "application/json");
        return connection;
    }
}
