package ru.yandex.incoming34.structures;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
@AllArgsConstructor
@Getter
@ToString
public class WeatherInfo {

    private final LocalDateTime localDateTime;
    private final JsonNode jsonNode;
    private final Pair<String, String> cordinates;
}
