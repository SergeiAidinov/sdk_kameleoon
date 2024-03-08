package ru.yandex.incoming34.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.yandex.incoming34.structures.Languages;
import ru.yandex.incoming34.structures.Metrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
public class Config {

    @Value("${app.cache.retention.timeInMinutes}")
    private String retention;
    private final Logger logger = Logger.getLogger(Config.class.getSimpleName());

    @Bean
    public Properties properties() throws IOException {

        final String propertiesFileName = "src/main/resources/application.properties";
        final File file = new File(propertiesFileName);
        final InputStream inputStream = new FileInputStream(file);
        final Properties properties = new Properties();
        properties.load(inputStream);
        try {
            if (!Arrays.stream(Languages.values()).toList().stream()
                    .map(language -> language.name()).collect(Collectors.toList())
                    .contains(properties.getProperty("app.language")))
                throw new RuntimeException("Unsupported language: " + properties.getProperty("app.language"));

            if (!Arrays.stream(Metrics.values()).toList().stream()
                    .map(unit -> unit.name()).collect(Collectors.toList())
                    .contains(properties.getProperty("app.units")))
                throw new RuntimeException("Unsupported measure unit: " + properties.getProperty("app.units"));
        } catch (RuntimeException runtimeException) {
            logger.log(Level.INFO, "Configuration error: " + runtimeException.getMessage());
            System.exit(1);
        }
        return properties;
    }
}
