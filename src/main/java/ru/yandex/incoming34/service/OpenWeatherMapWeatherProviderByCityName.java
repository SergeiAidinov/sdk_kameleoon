package ru.yandex.incoming34.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@AllArgsConstructor
@Service
public class OpenWeatherMapWeatherProviderByCityName implements WeatherProviderByCityName {

    private final SdkKameleoonCache sdkKameleoonCache;

    @Override
    public Optional<JsonNode> requestWeather(String cityName) {
        Optional<JsonNode> weatherOptional = sdkKameleoonCache.getActualWeather(cityName);
        return weatherOptional;
    }

}
