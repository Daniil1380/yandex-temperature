import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class YandexWeatherApp {

    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";
    private static final String API_KEY = "8e8b4434-447d-46b1-b0b0-b1de580ee698"; // Замените на ваш API ключ

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double lat = scanner.nextDouble();
        double lon = scanner.nextDouble();
        int limit = scanner.nextInt();

        try {
            String response = getWeatherData(lat, lon, limit);
            System.out.println("Full JSON response:");
            System.out.println(response);

            JSONObject jsonObject = new JSONObject(response);
            JSONObject fact = jsonObject.getJSONObject("fact");
            double currentTemp = fact.getDouble("temp");
            System.out.println("Current temperature: " + currentTemp);

            List<Double> temperatures = new ArrayList<>();
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

            double averageTemp = temperatures.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            System.out.println("Average temperature for " + limit + " days: " + averageTemp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getWeatherData(double lat, double lon, int limit) throws Exception {
        String url = API_URL + "?lat=" + lat + "&lon=" + lon + "&limit=" + limit;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("X-Yandex-Weather-Key", API_KEY);

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new RuntimeException("Failed to get response from the server. Response code: " + responseCode);
        }
    }
}