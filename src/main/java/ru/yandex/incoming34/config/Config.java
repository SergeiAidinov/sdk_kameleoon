package ru.yandex.incoming34.config;

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

@Configuration
@EnableScheduling
public class Config {

    @Value("${app.apiKey}")
    private String apiKey;
    @Value("${app.apiHttpCoordinates}")
    private String apiHttpCoordinates;
    @Value("${app.apihttpWeather}")
    private String apiHttpWeather;
    @Value("${app.cache.retention.timeInMinutes}")
    private String retention;
    @Value("${app.cache.size}")
    private String cacheSize;
    @Value("${app.cache.mode}")
    private String cacheMode;


    @Bean
    public Properties properties(){
        Properties properties = new Properties();
        properties.setProperty("apiKey", apiKey);
        properties.setProperty("apiHttpCoordinates", apiHttpCoordinates);
        properties.setProperty("apiHttpWeather", apiHttpWeather);
        properties.setProperty("retention", retention);
        properties.setProperty("cacheSize", cacheSize);
        properties.setProperty("cacheMode", cacheMode);
        return properties;
    }
}
