package ru.yandex.incoming34.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class Config {

    @Value("${app.apiKey}")
    private String apiKey;
    @Value("${app.apihttp}")
    private String apiHttp;

    @Bean
    public Properties properties(){
        Properties properties = new Properties();
        properties.setProperty("apiKey", apiKey);
        properties.setProperty("apiHttp", apiHttp);
        return properties;
    }
}
