package ru.yandex.incoming34.structures;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SdkKameleoonErrors { REQUEST_INVALID("Invalid user request"),
        NO_DATA("No Weather report provided"),
        WEATHER_SERVICE_UNAVAILABLE("Weather service is unavailable"),
        CITY_NOT_FOUND("City not found: ");

    private final String errorType;
}
