package com.example.widgetapp;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectFetch {
    private static final String LOG_TAG = "ConnectFetch";


    private static final String YANDEX_WEATHER_API =
            "https://api.weather.yandex.ru/v2/forecast?lat=%s&lon=%s&limit=1";


    private static final String[][] CITY_COORDINATES = {
            {"Orenburg", "51.7727", "55.0988"},
            {"Moscow", "55.7558", "37.6173"},
            {"Saint Petersburg", "59.9343", "30.3351"},
            {"Kazan", "55.7964", "49.1089"},
            {"Yekaterinburg", "56.8389", "60.6057"}
    };


    private static final String API_KEY = "448f6876-3262-4637-bedd-f415d41c5500";

    private OnConnectionCompleteListener listener;
    private Handler handler;

    public ConnectFetch(Context context, String city, OnConnectionCompleteListener listener) {
        this.listener = listener;
        this.handler = new Handler();
        updateWeatherData(city, context);
    }

    private void updateWeatherData(final String city, final Context context) {
        new Thread() {
            public void run() {
                final JSONObject json = getJSON(city);
                if (json == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            listener.onFail(city + " - информация не найдена");
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            listener.onSuccess(json);
                        }
                    });
                }
            }
        }.start();
    }


    public static JSONObject getJSON(String city) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            String lat = null;
            String lon = null;
            String foundCityName = null;

            for (String[] cityData : CITY_COORDINATES) {
                if (cityData[0].equalsIgnoreCase(city)) {
                    foundCityName = cityData[0];
                    lat = cityData[1];
                    lon = cityData[2];
                    break;
                }
            }

            if (lat == null) {
                foundCityName = CITY_COORDINATES[0][0];
                lat = CITY_COORDINATES[0][1];
                lon = CITY_COORDINATES[0][2];
            }

            String urlString = String.format(YANDEX_WEATHER_API, lat, lon);
            URL url = new URL(urlString);

            Log.d(LOG_TAG, "Запрос Яндекс.Погоды для: " + foundCityName + " URL: " + urlString);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("X-Yandex-API-Key", API_KEY);
            connection.setRequestProperty("User-Agent", "WeatherApp/1.0");

            connection.connect();

            int responseCode = connection.getResponseCode();
            Log.d(LOG_TAG, "Код ответа Яндекс.Погоды: " + responseCode);

            if (responseCode != 200) {
                Log.e(LOG_TAG, "Ошибка HTTP от Яндекс.Погоды: " + responseCode);

                try {
                    BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream()));
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        error.append(line);
                    }
                    errorReader.close();
                    Log.e(LOG_TAG, "Ошибка ответа: " + error.toString());
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Не удалось прочитать ошибку");
                }
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            Log.d(LOG_TAG, "Ответ получен: " + json.toString().substring(0, Math.min(100, json.length())) + "...");

            JSONObject data = new JSONObject(json.toString());
            data.put("requested_city", foundCityName);

            Log.i(LOG_TAG, "Данные успешно получены для: " + foundCityName);
            return data;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Ошибка при получении данных для города: " + city, e);
            return null;
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception e) {
                Log.w(LOG_TAG, "Ошибка закрытия reader", e);
            }
            try {
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                Log.w(LOG_TAG, "Ошибка отключения", e);
            }
        }
    }

    public static String[] getSupportedCities() {
        String[] cities = new String[CITY_COORDINATES.length];
        for (int i = 0; i < CITY_COORDINATES.length; i++) {
            cities[i] = CITY_COORDINATES[i][0];
        }
        return cities;
    }


    public static String getConditionText(String condition) {
        if (condition == null) return " Неизвестно";

        switch (condition) {
            case "clear": return " Ясно";
            case "partly-cloudy": return " Малооблачно";
            case "cloudy": return " Облачно";
            case "overcast": return " Пасмурно";
            case "drizzle": return " Морось";
            case "light-rain": return " Небольшой дождь";
            case "rain": return "Дождь";
            case "moderate-rain": return " Умеренный дождь";
            case "heavy-rain": return " Сильный дождь";
            case "continuous-heavy-rain": return " Ливень";
            case "showers": return " Ливень";
            case "wet-snow": return " Мокрый снег";
            case "light-snow": return " Небольшой снег";
            case "snow": return "Снег";
            case "snow-showers": return " Снегопад";
            case "hail": return " Град";
            case "thunderstorm": return " Гроза";
            case "thunderstorm-with-rain": return " Гроза с дождем";
            case "thunderstorm-with-hail": return " Гроза с градом";
            default: return " " + condition;
        }
    }


    public static String getTemperatureField(JSONObject json) {
        try {
            JSONObject fact = json.getJSONObject("fact");
            int temp = fact.getInt("temp");
            return temp + "°C";
        } catch (Exception e) {
            return "N/A";
        }
    }


    public static int getWeatherIcon(String condition) {
        if (condition == null) return android.R.drawable.ic_dialog_info;

        switch (condition) {
            case "clear":
                return android.R.drawable.star_big_on;
            case "partly-cloudy":
            case "cloudy":
                return android.R.drawable.ic_lock_idle_low_battery;
            case "overcast":
                return android.R.drawable.ic_lock_idle_lock;
            case "drizzle":
            case "light-rain":
            case "rain":
            case "moderate-rain":
            case "heavy-rain":
            case "continuous-heavy-rain":
            case "showers":
                return android.R.drawable.ic_delete;
            case "wet-snow":
            case "light-snow":
            case "snow":
            case "snow-showers":
                return android.R.drawable.ic_media_ff;
            case "thunderstorm":
            case "thunderstorm-with-rain":
            case "thunderstorm-with-hail":
                return android.R.drawable.ic_media_pause;
            default:
                return android.R.drawable.ic_dialog_info;
        }
    }

    public static String getWeatherEmojiText(String condition) {
        if (condition == null) return "❓ Неизвестно";

        switch (condition) {
            case "clear": return "☀️ Ясно";
            case "partly-cloudy": return "🌤 Малооблачно";
            case "cloudy": return "⛅️ Облачно";
            case "overcast": return "☁️ Пасмурно";
            case "drizzle": return "🌧 Морось";
            case "light-rain": return "🌦 Дождь";
            case "rain": return "🌧 Дождь";
            case "moderate-rain": return "🌧💧 Дождь";
            case "heavy-rain": return "🌧💦 Ливень";
            case "continuous-heavy-rain": return "🌧💦💦 Ливень";
            case "showers": return "🌦 Ливень";
            case "wet-snow": return "🌧❄️ Снег";
            case "light-snow": return "🌨 Снег";
            case "snow": return "🌨 Снег";
            case "snow-showers": return "🌨💨 Снегопад";
            case "hail": return "🌨🌀 Град";
            case "thunderstorm": return "⛈ Гроза";
            case "thunderstorm-with-rain": return "⛈🌧 Гроза";
            case "thunderstorm-with-hail": return "⛈🌨 Гроза";
            default: return "❓ " + condition;
        }
    }
}