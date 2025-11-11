package com.example.widgetapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONObject;

import java.util.Arrays;

public class AppWidget extends AppWidgetProvider {
    private static final String LOG_TAG = "AppWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Log.d(LOG_TAG, "onUpdate - widget IDs: " + Arrays.toString(appWidgetIds));

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.i(LOG_TAG, "onDeleted - removed widget IDs: " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.i(LOG_TAG, "onEnabled - first widget instance created");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.i(LOG_TAG, "onDisabled - last widget instance removed");
    }

    public static void pushWidgetUpdate(Context context, RemoteViews rv) {
        ComponentName myWidget = new ComponentName(context, AppWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, rv);
    }


    public static void renderWeather(JSONObject json, Context context, RemoteViews remoteViews, int appWidgetId) {
        try {
            JSONObject fact = json.getJSONObject("fact");
            int temp = fact.getInt("temp");
            String temperature = temp + "°C";
            remoteViews.setTextViewText(R.id.details_field, temperature);
            String condition = fact.getString("condition");
            int weatherIcon = ConnectFetch.getWeatherIcon(condition);
            remoteViews.setImageViewResource(R.id.weather_icon, weatherIcon);
            pushWidgetUpdate(context, remoteViews);

            Log.d(LOG_TAG, "Widget updated with Яндекс.Погода - temp: " + temperature + ", condition: " + condition);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Ошибка при обновлении виджета Яндекс.Погоды", e);
            remoteViews.setTextViewText(R.id.details_field, "Ошибка");
            remoteViews.setImageViewResource(R.id.weather_icon, android.R.drawable.ic_dialog_alert);
            pushWidgetUpdate(context, remoteViews);
        }
    }

    static void updateAppWidget(final Context context, AppWidgetManager appWidgetManager, final int appWidgetId) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        remoteViews.setTextViewText(R.id.details_field, "Загрузка...");
        remoteViews.setImageViewResource(R.id.weather_icon, android.R.drawable.ic_popup_sync);


        new ConnectFetch(context, "Orenburg", new OnConnectionCompleteListener() {
            @Override
            public void onSuccess(JSONObject response) {
                renderWeather(response, context, remoteViews, appWidgetId);
            }

            @Override
            public void onFail(String message) {
                remoteViews.setTextViewText(R.id.details_field, "Ошибка");
                remoteViews.setImageViewResource(R.id.weather_icon, android.R.drawable.ic_dialog_alert);
                pushWidgetUpdate(context, remoteViews);
                Log.e(LOG_TAG, "Failed to update widget with Яндекс.Погода: " + message);
            }
        });

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
}