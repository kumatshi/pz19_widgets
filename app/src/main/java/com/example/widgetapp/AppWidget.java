package com.example.widgetapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;

public class AppWidget extends AppWidgetProvider {
    private static final String LOG_TAG = "AppWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Log.d(LOG_TAG, "Обновление виджетов: " + Arrays.toString(appWidgetIds));

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.i(LOG_TAG, "Удалены виджеты: " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.i(LOG_TAG, "Первый виджет создан");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.i(LOG_TAG, "Последний виджет удален");
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
            views.setTextViewText(R.id.appwidget_text, " Яндекс Погода");

            appWidgetManager.updateAppWidget(appWidgetId, views);

            Log.d(LOG_TAG, "Виджет обновлен: " + appWidgetId);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Ошибка обновления виджета: " + appWidgetId, e);
        }
    }
}