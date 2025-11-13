package com.example.widgetapp;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity {
    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;
    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_CITY = "widget_city_";

    private Spinner citySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }


        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }


        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        setResult(RESULT_CANCELED, resultValue);

        setupCitySpinner();
    }

    private void setupCitySpinner() {
        citySpinner = findViewById(R.id.city_spinner);
        String[] cities = ConnectFetch.getSupportedCities();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cities
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);
    }

    public void saveWidgetSettings(View view) {
        String selectedCity = citySpinner.getSelectedItem().toString();

        if (selectedCity.isEmpty()) {
            Toast.makeText(this, "Выберите город", Toast.LENGTH_SHORT).show();
            return;
        }


        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(WIDGET_CITY + widgetID, selectedCity);
        editor.apply();


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        AppWidget.updateAppWidget(this, sp, appWidgetManager, widgetID);


        setResult(RESULT_OK, resultValue);

        Toast.makeText(this, "Город сохранен: " + selectedCity, Toast.LENGTH_SHORT).show();
        finish();
    }
}