package ru.yandex.incoming34.test_task_for_employment.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.incoming34.test_task_for_employment.service.MainService;
import ru.yandex.incoming34.test_task_for_employment.service.ValidationService;
import ru.yandex.incoming34.test_task_for_employment.structures.AdaptedMessage;
import ru.yandex.incoming34.test_task_for_employment.structures.ServiceAMessage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/message_from_service_a")
@AllArgsConstructor
public class Controller {

    private final MainService mainService;
    private final ValidationService validationService;
    private final Logger logger = Logger.getLogger(Controller.class.getSimpleName());

    @PostMapping(value = "/new_message")
    @Operation(description = "Эндпойнт, вызываемый гипотетическим Сервисом А, и принимающий от него сообщения для последующей обработки Адаптером.")
    public AdaptedMessage handleMessageFromServiceA(@RequestBody ServiceAMessage serviceAMessage) {
        validationService.throwExceptionIfInvalid(serviceAMessage);
        return mainService.handleMessageFromServiceA(serviceAMessage);
    }

    @ExceptionHandler(value = Exception.class)
    private ResponseEntity<String> handleException(RuntimeException exception) {
        final String message = Objects.nonNull(exception.getMessage()) ? exception.getMessage() : "Unknown Error";
        logger.log(Level.INFO, message);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

}
