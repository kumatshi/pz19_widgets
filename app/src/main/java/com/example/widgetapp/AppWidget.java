package com.example.widgetapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONObject;

import java.util.Arrays;

public class AppWidget extends AppWidgetProvider {
    private static final String LOG_TAG = "AppWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Log.d(LOG_TAG, "=== WIDGET UPDATE STARTED ===");
        Log.d(LOG_TAG, "Widget IDs: " + Arrays.toString(appWidgetIds));
        for (int appWidgetId : appWidgetIds) {
            setTestData(context, appWidgetId);
        }

        SharedPreferences sp = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, sp, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(ConfigActivity.WIDGET_CITY + widgetID);
        }
        editor.apply();

        Log.i(LOG_TAG, "Widgets deleted: " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.i(LOG_TAG, "First widget created");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.i(LOG_TAG, "Last widget removed");
    }


    private static void setTestData(Context context, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        remoteViews.setTextViewText(R.id.city_field, "Оренбург");
        remoteViews.setTextViewText(R.id.temp_field, "2°C");
        remoteViews.setTextViewText(R.id.weather_field, "Облачно");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        Log.d(LOG_TAG, "Test data set for widget: " + appWidgetId);
    }

    public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        ComponentName myWidget = new ComponentName(context, AppWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, remoteViews);
        Log.d(LOG_TAG, "Widget pushed to update");
    }

    public static void renderWeather(JSONObject json, Context context, RemoteViews remoteViews, int appWidgetId) {
        try {
            Log.d(LOG_TAG, "Starting weather rendering");

            JSONObject fact = json.getJSONObject("fact");
            String cityName = json.getString("requested_city");
            int temp = fact.getInt("temp");
            String condition = fact.getString("condition");

            Log.d(LOG_TAG, "Data: " + cityName + ", " + temp + "°C, " + condition);

            String weatherText = getWeatherText(condition);

            remoteViews.setTextViewText(R.id.city_field, cityName);
            remoteViews.setTextViewText(R.id.temp_field, temp + "°C");
            remoteViews.setTextViewText(R.id.weather_field, weatherText);

            pushWidgetUpdate(context, remoteViews);

            Log.d(LOG_TAG, "=== WIDGET UPDATED SUCCESSFULLY ===");
            Log.d(LOG_TAG, "City: " + cityName);
            Log.d(LOG_TAG, "Temp: " + temp + "°C");
            Log.d(LOG_TAG, "Weather: " + weatherText);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error rendering weather: " + e.getMessage(), e);

            remoteViews.setTextViewText(R.id.city_field, "Ошибка");
            remoteViews.setTextViewText(R.id.temp_field, "--°C");
            remoteViews.setTextViewText(R.id.weather_field, "Данные не получены");

            pushWidgetUpdate(context, remoteViews);
        }
    }

    private static String getWeatherText(String condition) {
        if (condition == null) return "Неизвестно";

        switch (condition) {
            case "clear": return "Ясно";
            case "partly-cloudy":
            case "cloudy": return "Облачно";
            case "overcast": return "Пасмурно";
            case "drizzle":
            case "light-rain":
            case "rain":
            case "moderate-rain": return "Дождь";
            case "heavy-rain":
            case "continuous-heavy-rain":
            case "showers": return "Ливень";
            case "wet-snow":
            case "light-snow":
            case "snow":
            case "snow-showers": return "Снег";
            case "hail": return "Град";
            case "thunderstorm":
            case "thunderstorm-with-rain":
            case "thunderstorm-with-hail": return "Гроза";
            default: return condition;
        }
    }

    static void updateAppWidget(final Context context, SharedPreferences sharedPreferences,
                                AppWidgetManager appWidgetManager, final int appWidgetId) {
        Log.d(LOG_TAG, "Updating widget: " + appWidgetId);

        String widgetCity = sharedPreferences.getString(ConfigActivity.WIDGET_CITY + appWidgetId, "Orenburg");
        Log.d(LOG_TAG, "City for widget: " + widgetCity);

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        remoteViews.setTextViewText(R.id.city_field, widgetCity);
        remoteViews.setTextViewText(R.id.temp_field, "...");
        remoteViews.setTextViewText(R.id.weather_field, "Загрузка...");

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        Log.d(LOG_TAG, "Loading state set, fetching weather...");

        new ConnectFetch(context, widgetCity, new OnConnectionCompleteListener() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(LOG_TAG, "Weather data received successfully");
                renderWeather(response, context, remoteViews, appWidgetId);
            }

            @Override
            public void onFail(String message) {
                Log.e(LOG_TAG, "Weather fetch failed: " + message);
                remoteViews.setTextViewText(R.id.city_field, widgetCity);
                remoteViews.setTextViewText(R.id.temp_field, "Ошибка");
                remoteViews.setTextViewText(R.id.weather_field, "Нет связи");
                pushWidgetUpdate(context, remoteViews);
            }
        });
    }
}