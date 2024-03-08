package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.Pair;
import ru.yandex.incoming34.structures.UserRequest;

import java.util.Optional;

public interface WeatherProvider {
    Optional<JsonNode> requestWeather(String cityName);
    //Optional<JsonNode>  findWeatherByCoordinates(Pair<String, String> coordinates, String cityName);
}
