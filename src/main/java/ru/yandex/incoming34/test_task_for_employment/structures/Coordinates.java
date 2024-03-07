package ru.yandex.incoming34.test_task_for_employment.structures;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static ru.yandex.incoming34.test_task_for_employment.structures.RegExp.latitudeRegExp;
import static ru.yandex.incoming34.test_task_for_employment.structures.RegExp.longitudeRegExp;

@AllArgsConstructor
@Getter
public class Coordinates {

    @Pattern(regexp = latitudeRegExp)
    private final String latitude;
    @Pattern(regexp =longitudeRegExp)
    private final String longitude;
}
