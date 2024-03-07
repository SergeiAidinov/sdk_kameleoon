package ru.yandex.incoming34.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.incoming34.service.ValidationService;
import ru.yandex.incoming34.service.WeatherProvider;
import ru.yandex.incoming34.structures.UserRequest;

import java.util.Optional;

import static ru.yandex.incoming34.controller.SdkKameleoonControllerExceptionHandler.sdkKameleoonErrors;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@SdkKameleoonControllerExceptionHandler
public class Controller {

    private final WeatherProvider weatherProvider;
    private final ValidationService validationService;

    @GetMapping(value = "/new_weather_report")
    @Operation(description = "Эндпойнт, вызываемый гипотетическим Сервисом А, и принимающий от него сообщения для последующей обработки Адаптером.")
    public JsonNode handleMessageFromServiceA(String cityName) {
        Optional<JsonNode> responseOptional = weatherProvider.requestWeather(cityName);
        if (responseOptional.isPresent()) return responseOptional.get();
        throw new RuntimeException(sdkKameleoonErrors.get("NO_DATA"));
    }
}
