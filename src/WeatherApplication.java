import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherApplication {
    private static final String API_URL =
            "https://api.weather.yandex.ru/v2/forecast?lat=44.82&lon=20.46&limit=3";

    public static void main(String[] args) throws IOException, InterruptedException {
        String apiKey = System.getenv("YANDEX_WEATHER_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Missing env var YANDEX_WEATHER_API_KEY");
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder();

        builder.uri(URI.create(API_URL));
        builder.header("X-Yandex-Weather-Key", apiKey);
        builder.GET();

        HttpRequest request = builder.build();

        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                String json = response.body();
                System.out.println(json);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(json);

                int currentTemperature = root.get("fact").get("temp").asInt();

                double temperatureSum = 0;
                int forecastCount = 0;

                for (JsonNode forecast : root.get("forecasts")) {
                    JsonNode dayTemperature = forecast.get("parts").get("day").get("temp_avg");

                    temperatureSum += dayTemperature.asDouble();
                    forecastCount++;
                }

                double averageTemperature = temperatureSum / forecastCount;

                System.out.println("Current temp: " + currentTemperature + " celsius");
                System.out.printf("Avg daytime temp: %.2f celsius%n", averageTemperature);
            } else {
                System.err.println("HTTP err code: " + response.statusCode());
                System.err.println(response.body());
            }
        } finally {
            client.close();
        }
    }
}
