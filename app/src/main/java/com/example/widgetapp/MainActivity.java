package com.example.widgetapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private Button changeCityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "Инициализация приложения с настройками города");
        initializeViews();
        setupCitySpinner();
        setupRefreshButton();
        setupChangeCityButton();

        setInfo();
    }

    private void initializeViews() {
        weatherText = findViewById(R.id.weather);
        weatherDetails = findViewById(R.id.weather_details);
        cityInfo = findViewById(R.id.city_info);
        refreshButton = findViewById(R.id.refresh_button);
        citySpinner = findViewById(R.id.city_spinner);
        changeCityButton = findViewById(R.id.change_city_button);
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
    }

    private void setupRefreshButton() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedCity = citySpinner.getSelectedItem().toString();
                changeCity(selectedCity);
                Toast.makeText(MainActivity.this,
                        "Обновляем погоду для " + selectedCity,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupChangeCityButton() {
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });
    }

    private void setInfo() {
        String city = new CityPreference(this).getCity();
        loadWeatherData(city);
    }

    public void changeCity(String city) {
        new CityPreference(this).setCity(city);
        setInfo();
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Измените город:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(new CityPreference(this).getCity());
        builder.setView(input);

        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void loadWeatherData(String city) {
        weatherText.setText("⏳ Загрузка...");
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
                    "💨 Ощущается как: %d°C\n" +
                            "💧 Влажность: %d%%\n" +
                            "📊 Давление: %d мм\n" +
                            "🌬 Ветер: %.1f м/с",
                    feelsLike, humidity, pressure, windSpeed
            );
            weatherDetails.setText(detailsText);

            String cityText = String.format("📍 %s\n🕐 %s\n⏰ %s", cityName, updatedOn, timezone);
            cityInfo.setText(cityText);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Ошибка отображения данных", e);
            showError("Ошибка обработки данных");
        }
    }

    private void showError(String message) {
        weatherText.setText("❌ Ошибка");
        weatherDetails.setText(message);
        cityInfo.setText("Попробуйте обновить");
    }
}