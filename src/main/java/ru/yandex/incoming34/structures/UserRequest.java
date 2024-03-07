package ru.yandex.incoming34.structures;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserRequest {

    private final Languages lng;
    @Valid
    private final Coordinates coordinates;
}
