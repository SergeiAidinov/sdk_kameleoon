package ru.yandex.incoming34.service;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.structures.SdkKameleoonErrors;
import ru.yandex.incoming34.structures.UserRequest;

@Service
@RequiredArgsConstructor
public class ValidationService {
   /* public static final Map<String, String> sdkKameleoonErrors = Map.of(
            "REQUEST_INVALID", "Invalid user request"
    );*/
    private final Validator validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();

    public void throwExceptionIfInvalid(UserRequest userRequest) {
        if(!validator.validate(userRequest).isEmpty()) throw  new RuntimeException(SdkKameleoonErrors.REQUEST_INVALID.getErrorType());
    }

}
