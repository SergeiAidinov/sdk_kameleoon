package ru.yandex.incoming34.controller;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SdkKameleoonControllerExceptionHandler {

    Map<String, String> sdkKameleoonErrors = Map.of(
            "REQUEST_INVALID", "Invalid user request",
            "NO_DATA", "No Weather report provided",
            "WEATHER_SERVICE_UNAVAILABLE", "Weather service is unavailable",
            "CITY_NOT_FOUND", "City not found: "
    );
}
