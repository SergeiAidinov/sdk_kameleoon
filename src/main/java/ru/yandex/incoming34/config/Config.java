package ru.yandex.incoming34.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class Config {

    @Value("${app.apiKey}")
    private String apiKey;
    @Value("${app.apiHttpCoordinates}")
    private String apiHttpCoordinates;
    @Value("${app.apihttpWeather}")
    private String apiHttpWeather;

    @Bean
    public Properties properties(){
        Properties properties = new Properties();
        properties.setProperty("apiKey", apiKey);
        properties.setProperty("apiHttpCoordinates", apiHttpCoordinates);
        properties.setProperty("apiHttpWeather", apiHttpWeather);
        return properties;
    }
}
