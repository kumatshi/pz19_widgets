package com.example.widgetapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "WeatherApp";

    private TextView weatherText;
    private TextView weatherDetails;
    private TextView cityInfo;
    private Button refreshButton;
    private Spinner citySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "Запуск Яндекс.Погода приложения");
        initializeViews();
        setupCitySpinner();
        setupRefreshButton();

        updateWeatherData("Orenburg");
    }

    private void initializeViews() {
        weatherText = findViewById(R.id.weather);
        weatherDetails = findViewById(R.id.weather_details);
        cityInfo = findViewById(R.id.city_info);
        refreshButton = findViewById(R.id.refresh_button);
        citySpinner = findViewById(R.id.city_spinner);
    }

    private void setupCitySpinner() {
        String[] cities = ConnectFetch.getSupportedCities();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cities
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);

        Log.d(LOG_TAG, "Список городов загружен: " + cities.length + " городов");
    }

    private void setupRefreshButton() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedCity = citySpinner.getSelectedItem().toString();
                updateWeatherData(selectedCity);
                Toast.makeText(MainActivity.this,
                        "Обновляем погоду для " + selectedCity,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWeatherData(final String city) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                weatherText.setText(" Загрузка...");
                weatherDetails.setText("Подключаемся к Яндекс.Погоде");
                cityInfo.setText("Город: " + city);
            }
        });

        new Thread() {
            public void run() {
                Log.i(LOG_TAG, "=== ЗАПРОС ПОГОДЫ ===");
                Log.i(LOG_TAG, "Город: " + city);

                final JSONObject json = ConnectFetch.getJSON(city);

                runOnUiThread(new Runnable() {
                    public void run() {
                        if (json == null) {
                            showError();
                        } else {
                            renderWeather(json);
                        }
                    }
                });
            }
        }.start();
    }

    private void renderWeather(JSONObject json) {
        try {
            Log.d(LOG_TAG, "Начинаем обработку JSON ответа");

            // Основные данные
            JSONObject fact = json.getJSONObject("fact");
            String cityName = json.getString("requested_city");

            // Получаем числовые значения
            int temp = fact.getInt("temp");
            int feelsLike = fact.getInt("feels_like");
            int humidity = fact.getInt("humidity");
            int pressure = fact.getInt("pressure_mm");
            double windSpeed = fact.getDouble("wind_speed");

            // Получаем условие как СТРОКУ
            String conditionString = fact.getString("condition");
            String condition = ConnectFetch.getConditionText(conditionString);

            // Время - используем поле "now" вместо "now_ts"
            long timestamp = json.getLong("now") * 1000; // Исправлено здесь
            DateFormat df = DateFormat.getDateTimeInstance();
            String updateTime = df.format(new Date(timestamp));

            // Получаем информацию о часовом поясе
            JSONObject info = json.getJSONObject("info");
            JSONObject tzinfo = info.getJSONObject("tzinfo");
            String timezone = tzinfo.getString("name");

            // Обновляем интерфейс
            String weatherDisplay = String.format("%s\n🌡 %d°C", condition, temp);
            weatherText.setText(weatherDisplay);

            String detailsText = String.format(
                    " Ощущается как: %d°C\n" +
                            " Влажность: %d%%\n" +
                            " Давление: %d мм\n" +
                            " Ветер: %.1f м/с",
                    feelsLike, humidity, pressure, windSpeed
            );
            weatherDetails.setText(detailsText);

            String cityText = String.format(" %s\n %s\n %s", cityName, updateTime, timezone);
            cityInfo.setText(cityText);

            Log.i(LOG_TAG, "Погода отображена: " + cityName + " " + temp + "°C, условие: " + conditionString);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Ошибка отображения погоды", e);

            // Детальный лог ошибки
            try {
                Log.e(LOG_TAG, "JSON ключи: " + json.toString().substring(0, 200) + "...");
            } catch (Exception logEx) {
                Log.e(LOG_TAG, "Не удалось записать JSON для отладки");
            }

            showError();
        }
    }

    private void showError() {
        weatherText.setText(" Ошибка");
        weatherDetails.setText("Проверьте:\n• Интернет соединение\n• API ключ\n• Город");
        cityInfo.setText("Попробуйте другой город");
        Toast.makeText(this, "Ошибка загрузки погоды", Toast.LENGTH_LONG).show();
    }
}