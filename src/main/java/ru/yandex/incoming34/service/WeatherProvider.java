package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.yandex.incoming34.structures.UserRequest;

import java.util.Optional;

public interface WeatherProvider {
    Optional<JsonNode> requestWeather(UserRequest userRequest);
}
