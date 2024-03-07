package ru.yandex.incoming34.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import ru.yandex.incoming34.structures.AdaptedMessage;

@RestController
@RequestMapping("dummy_service_b/api/adapted_message")
public class DummyServiceBController {

    @PutMapping(value = "/new_message")
    @Operation(description = "Эндпойнт, имитирующий получение обработанных сообщений Сервисом Б. " +
            "Необходим исключительно для демонстрации работы RestTemplate в WheatherService." +
            "Полученные сообщения выводятся в консоль без какой - либо обработки.")
    public void handleAdaptedMessage(@RequestBody AdaptedMessage adaptedMessage) {
        System.out.println(adaptedMessage);
    }

}
