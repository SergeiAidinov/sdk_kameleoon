package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface MainService {
    Optional<JsonNode> getActualWeather(String cityName);
}
