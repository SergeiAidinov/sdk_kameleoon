package ru.yandex.incoming34.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.incoming34.exception.SdkKameleoonException;
import ru.yandex.incoming34.service.WeatherProviderByCityName;
import ru.yandex.incoming34.structures.SdkKameleoonErrors;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@SdkKameleoonControllerExceptionHandler
public class Controller {

    private final WeatherProviderByCityName weatherProviderByCityName;

    @GetMapping(value = "/new_weather_report")
    @Operation(description = "Endpoint accepts the name of the city and return information about the weather at the current moment.")
    public JsonNode handleMessageFromServiceA(@Schema(example = "London") String cityName) {
        Optional<JsonNode> responseOptional = weatherProviderByCityName.requestWeather(cityName);
        if (responseOptional.isPresent()) {
            return responseOptional.get();
        } else
            throw new SdkKameleoonException(SdkKameleoonErrors.NO_DATA);
    }
}
