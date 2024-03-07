package ru.yandex.incoming34.test_task_for_employment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.incoming34.test_task_for_employment.service.OpenWeatherMapTemperatureProvider;
import ru.yandex.incoming34.test_task_for_employment.service.TemperatureProvider;

import java.util.List;
import java.util.Properties;

@Configuration
public class Config {

    @Value("${app.supportedlanguages}")
    private List<String> langList;
    @Value("${app.appid}")
    private String appId;
    @Value("${app.apihttp}")
    private String apiHttp;
    @Value("${app.dummyurl}")
    private String dummyUrl;
    @Value("${app.nodes}")
    private List<String> nodeList;


    @Bean
    public List<String> supportedlanguages(){
        return langList;
    }

    @Bean
    public List<String> nodeList(){
        return nodeList;
    }

    @Bean
    public Properties properties(){
        Properties properties = new Properties();
        properties.setProperty("appId", appId);
        properties.setProperty("apiHttp", apiHttp);
        properties.setProperty("dummyUrl", dummyUrl);
        return properties;
    }

    @Bean(name = "TempProvider")
    @ConditionalOnProperty(prefix = "app", name = "weather_provider", havingValue = "OpenWeatherMap")
    public TemperatureProvider temperatureProvider(){
        return new OpenWeatherMapTemperatureProvider(nodeList, properties());
    }

}
