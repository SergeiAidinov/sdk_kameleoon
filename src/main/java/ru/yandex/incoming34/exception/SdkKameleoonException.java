package ru.yandex.incoming34.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.yandex.incoming34.structures.SdkKameleoonErrors;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class SdkKameleoonException extends RuntimeException {

    private final SdkKameleoonErrors sdkKameleoonErrors;
    private String details;
}
