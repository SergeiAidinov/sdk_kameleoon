package ru.yandex.incoming34.structures;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Coordinates {

    @Pattern(regexp = RegExp.latitudeRegExp)
    private final String latitude;
    @Pattern(regexp = RegExp.longitudeRegExp)
    private final String longitude;
}
