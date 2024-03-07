package ru.yandex.incoming34.test_task_for_employment.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.incoming34.test_task_for_employment.structures.AdaptedMessage;
import ru.yandex.incoming34.test_task_for_employment.structures.ServiceAMessage;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;

@Service
@AllArgsConstructor
public class MainService {


    private final Properties properties;
    @Qualifier(value = "TempProvider")
    private final TemperatureProvider temperatureProvider;
    private final RestTemplate dummyRestTemplate = new RestTemplate();


    public AdaptedMessage handleMessageFromServiceA(ServiceAMessage serviceAMessage) {
        Optional<AdaptedMessage> adaptedMessageOptional = temperatureProvider.requestTemperature(serviceAMessage.getCoordinates())
                .map(temperature -> adaptMessage(serviceAMessage, temperature));

        adaptedMessageOptional.ifPresentOrElse(adaptedMessage -> {
            try {
                dummyRestTemplate.put(properties.getProperty("dummyUrl"), adaptedMessage);
            } catch (Exception e) {
                throw new RuntimeException("Не удалось вызвать Service B");
            }
        }, () -> {
            throw new RuntimeException("Не удалось получить значение температуры");
        });
        return adaptedMessageOptional.get();
    }

    private AdaptedMessage adaptMessage(ServiceAMessage serviceAMessage, String temperature) {
        return new AdaptedMessage(serviceAMessage.getMsg(), LocalDateTime.now(), temperature);
    }
}
