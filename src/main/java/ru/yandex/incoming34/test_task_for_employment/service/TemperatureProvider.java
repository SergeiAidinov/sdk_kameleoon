package ru.yandex.incoming34.test_task_for_employment.service;

import ru.yandex.incoming34.test_task_for_employment.structures.Coordinates;

import java.util.Optional;

public interface TemperatureProvider {

    Optional<String> requestTemperature(Coordinates coordinates);
}
