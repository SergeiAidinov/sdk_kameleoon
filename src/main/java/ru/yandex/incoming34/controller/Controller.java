package ru.yandex.incoming34.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.incoming34.exception.SdkKameleoonException;
import ru.yandex.incoming34.service.MainService;
import ru.yandex.incoming34.structures.SdkKameleoonErrors;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@SdkKameleoonControllerExceptionHandler
public class Controller {

    private final MainService mainService;

    @GetMapping(value = "/new_weather_report")
    @Operation(description = "Endpoint accepts the name of the city and return information about the weather at the current moment.")
    public JsonNode getWeather(@Schema(example = "London") String cityName) {
        return mainService.getActualWeather(cityName).orElseThrow(() -> new SdkKameleoonException(SdkKameleoonErrors.NO_DATA));
    }
}
