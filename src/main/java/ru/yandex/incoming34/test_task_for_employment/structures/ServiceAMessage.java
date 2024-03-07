package ru.yandex.incoming34.test_task_for_employment.structures;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ServiceAMessage {

    @NotBlank
    private final String msg;
    private final Languages lng;
    @Valid
    private final Coordinates coordinates;
}
