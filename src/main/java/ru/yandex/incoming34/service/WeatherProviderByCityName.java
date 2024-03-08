package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface WeatherProviderByCityName {
    Optional<JsonNode> requestWeather(String cityName);
    //Optional<JsonNode>  findWeatherByCoordinates(Pair<String, String> coordinates, String cityName);
}
