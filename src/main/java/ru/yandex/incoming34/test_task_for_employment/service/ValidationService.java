package ru.yandex.incoming34.test_task_for_employment.service;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.test_task_for_employment.structures.ServiceAMessage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final List<String> supportedlanguages;
    private final String UNSUPPORTED_LANGUAGE = "Неподдерживаемый язык: ";
    private final String MESSAGE_INVALID = "Неверный формат сообщения";
    private final Validator validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();

    public void throwExceptionIfInvalid(ServiceAMessage serviceAMessage) {
        if(!validator.validate(serviceAMessage).isEmpty()) throw  new RuntimeException(MESSAGE_INVALID);
        if (!supportedlanguages.contains(serviceAMessage.getLng().name())) throw new RuntimeException(UNSUPPORTED_LANGUAGE + serviceAMessage.getLng());
    }

}
