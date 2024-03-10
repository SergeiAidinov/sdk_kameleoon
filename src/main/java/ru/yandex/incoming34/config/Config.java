package ru.yandex.incoming34.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.yandex.incoming34.structures.CacheMode;
import ru.yandex.incoming34.structures.Languages;
import ru.yandex.incoming34.structures.Metrics;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableScheduling
public class Config {

    @Value("${app.cache.retention.timeInMinutes}")
    private String retention;
    private final Logger logger = Logger.getLogger(Config.class.getSimpleName());

    @Bean
    public Properties properties() throws IOException {
        final File file = findFile("application.properties");
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
            if (!Arrays.stream(CacheMode.values()).toList().stream()
                    .map(unit -> unit.name()).collect(Collectors.toList())
                    .contains(properties.getProperty("app.cache.mode")))
                throw new RuntimeException("Unsupported cache mode: " + properties.getProperty("app.cache.mode"));
        } catch (RuntimeException runtimeException) {
            logger.log(Level.INFO, "Configuration error: " + runtimeException.getMessage());
            System.exit(1);
        }
        return properties;
    }

    private File findFile(String fileName) throws FileNotFoundException {
        try {
            Stream<Path> walkStream = Files.walk(Paths.get(System.getProperty("user.dir")));
            List<Path> fileList = walkStream.filter(p -> p.toFile().isFile()).collect(Collectors.toList());
            for (Path path : fileList)
                if (path.getFileName().endsWith(fileName)) return new File(path.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new FileNotFoundException(fileName);
    }
}
