package ru.yandex.incoming34.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.incoming34.exception.SdkKameleoonException;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;



@ControllerAdvice(annotations = SdkKameleoonControllerExceptionHandler.class)
public class SdkKameleoonAdvice {

    private final Logger logger = Logger.getLogger(Controller.class.getSimpleName());

    @ExceptionHandler(SdkKameleoonException.class)
    public ResponseEntity<String> handleException(SdkKameleoonException sdkKameleoonException) {
        final String message = Objects.nonNull(sdkKameleoonException.getDetails())
                ? sdkKameleoonException.getSdkKameleoonErrors().getErrorType() + sdkKameleoonException.getDetails()
                : sdkKameleoonException.getSdkKameleoonErrors().getErrorType();
        logger.log(Level.INFO, message);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
