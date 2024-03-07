package ru.yandex.incoming34.structures;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserRequest {

    private final Languages lng;
    private final Metrics metrics;
    @Valid
    private final Coordinates coordinates;
}
