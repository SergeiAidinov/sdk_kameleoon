package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public interface WeatherProviderByCoordinates {
    Optional<JsonNode> findWeatherByCoordinates(Pair<String, String> coordinates, String cityName);
}
