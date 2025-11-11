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

        Log.d(LOG_TAG, "Инициализация Яндекс.Погода приложения с анонимными классами");
        initializeViews();
        setupCitySpinner();
        setupRefreshButton();


        loadWeatherData("Orenburg");
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
                loadWeatherData(selectedCity);
                Toast.makeText(MainActivity.this,
                        "Обновляем погоду для " + selectedCity,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWeatherData(String city) {

        weatherText.setText(" Загрузка...");
        weatherDetails.setText("Подключаемся к Яндекс.Погоде");
        cityInfo.setText("Город: " + city);


        new ConnectFetch(this, city, new OnConnectionCompleteListener() {
            @Override
            public void onSuccess(JSONObject response) {
                renderWeather(response);
            }

            @Override
            public void onFail(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                showError(message);
            }
        });
    }

    private void renderWeather(JSONObject json) {
        try {
            Log.d(LOG_TAG, "Начинаем обработку JSON ответа от Яндекс.Погоды");


            JSONObject fact = json.getJSONObject("fact");
            String cityName = json.getString("requested_city");


            int temp = fact.getInt("temp");
            int feelsLike = fact.getInt("feels_like");
            int humidity = fact.getInt("humidity");
            int pressure = fact.getInt("pressure_mm");
            double windSpeed = fact.getDouble("wind_speed");


            String conditionString = fact.getString("condition");
            String condition = ConnectFetch.getConditionText(conditionString);


            long timestamp = json.getLong("now") * 1000;
            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(timestamp));


            JSONObject info = json.getJSONObject("info");
            JSONObject tzinfo = info.getJSONObject("tzinfo");
            String timezone = tzinfo.getString("name");


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

            String cityText = String.format(" %s\n🕐 %s\n⏰ %s", cityName, updatedOn, timezone);
            cityInfo.setText(cityText);

            Log.i(LOG_TAG, "Погода от Яндекс.Погоды отображена: " + cityName + " " + temp + "°C, условие: " + conditionString);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Ошибка отображения данных Яндекс.Погоды", e);


            try {
                Log.e(LOG_TAG, "JSON ключи: " + json.toString().substring(0, Math.min(200, json.toString().length())) + "...");
            } catch (Exception logEx) {
                Log.e(LOG_TAG, "Не удалось записать JSON для отладки");
            }

            showError("Ошибка обработки данных Яндекс.Погоды");
        }
    }

    private void showError(String message) {
        weatherText.setText(" Ошибка");
        weatherDetails.setText(message);
        cityInfo.setText("Попробуйте обновить");
    }
}