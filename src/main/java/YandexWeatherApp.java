import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class YandexWeatherApp {
    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";
    private static final String API_KEY = "8e8b4434-447d-46b1-b0b0-b1de580ee698"; // Замените на ваш API ключ

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double latitude = scanner.nextDouble();
        double longitude = scanner.nextDouble();
        int daysLimit = scanner.nextInt();

        try {
            String weatherData = getWeatherData(latitude, longitude, daysLimit);
            printFullJsonResponse(weatherData);

            List<Double> temperatures = getTemperatures(weatherData);
            printCurrentTemperature(temperatures);
            printAverageTemperature(temperatures, daysLimit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getWeatherData(double latitude, double longitude, int daysLimit) throws Exception {
        String url = API_URL + "?lat=" + latitude + "&lon=" + longitude + "&limit=" + daysLimit;
        return sendHttpRequest(url);
    }

    private static String sendHttpRequest(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-Yandex-Weather-Key", API_KEY);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return readResponseBody(connection);
        } else {
            throw new RuntimeException("Failed to get response from the server. Response code: " + responseCode);
        }
    }

    private static String readResponseBody(HttpURLConnection connection) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return in.lines().collect(Collectors.joining());
    }

    private static void printFullJsonResponse(String weatherData) {
        System.out.println("Full JSON response:");
        System.out.println(weatherData);
    }

    private static List<Double> getTemperatures(String weatherData) {
        List<Double> temperatures = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(weatherData);
        JSONObject fact = jsonObject.getJSONObject("fact");
        double currentTemp = fact.getDouble("temp");
        temperatures.add(currentTemp);

        if (jsonObject.has("forecasts")) {
            JSONArray forecasts = jsonObject.getJSONArray("forecasts");
            for (int i = 0; i < forecasts.length(); i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                JSONObject parts = forecast.getJSONObject("parts");
                JSONObject day = parts.getJSONObject("day");
                double temp = day.getDouble("temp_avg");
                temperatures.add(temp);
            }
        }
        return temperatures;
    }

    private static void printCurrentTemperature(List<Double> temperatures) {
        System.out.println("Current temperature: " + temperatures.get(0));
    }

    private static void printAverageTemperature(List<Double> temperatures, int daysLimit) {
        double averageTemp = temperatures.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        System.out.println("Average temperature for " + daysLimit + " days: " + averageTemp);
    }
}