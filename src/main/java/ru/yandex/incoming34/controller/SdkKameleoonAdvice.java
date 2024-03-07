package ru.yandex.incoming34.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.yandex.incoming34.controller.SdkKameleoonControllerExceptionHandler.sdkKameleoonErrors;


@ControllerAdvice(annotations = SdkKameleoonControllerExceptionHandler.class)
public class SdkKameleoonAdvice {

    private final Logger logger = Logger.getLogger(Controller.class.getSimpleName());

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        final String message = sdkKameleoonErrors.values().contains(exception.getMessage()) ? exception.getMessage() : "Unknown error";
        logger.log(Level.INFO, message);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
