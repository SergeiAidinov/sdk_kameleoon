package ru.yandex.incoming34.utils;

import ru.yandex.incoming34.exception.SdkKameleoonException;
import ru.yandex.incoming34.structures.SdkKameleoonErrors;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {

   public static HttpURLConnection getHttpURLConnection(String request) {
        HttpURLConnection connection;
        try {
            URL url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new SdkKameleoonException(SdkKameleoonErrors.WEATHER_SERVICE_UNAVAILABLE);
        }
        connection.setRequestProperty("accept", "application/json");
        return connection;
    }
}
