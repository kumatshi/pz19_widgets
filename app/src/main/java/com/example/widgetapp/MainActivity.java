package com.example.widgetapp;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "myLogs";
    private TextView widgetStatusText;
    private Button updateWidgetButton;
    private Button addWidgetButton;
    private Button openSettingsButton;
    private Button showInstructionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
        checkWidgetStatus();

        Log.d(LOG_TAG, "MainActivity created");
    }
     private void initializeViews() {
        widgetStatusText = findViewById(R.id.widget_status_text);
        updateWidgetButton = findViewById(R.id.update_widget_button);
        addWidgetButton = findViewById(R.id.add_widget_button);
//        openSettingsButton = findViewById(R.id.open_settings_button);
//        showInstructionsButton = findViewById(R.id.show_instructions_button);
    }

    private void setupClickListeners() {
        updateWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAllWidgets();
            }
        });
        addWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWidgetToHomeScreen();
            }
        });
        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppSettings();
            }
        });
        showInstructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailedInstructions();
            }
        });
    }
    private void addWidgetToHomeScreen() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Для Android 8.0 и выше
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
                        new ComponentName(this, AppWidget.class));
                startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.setAction("android.appwidget.action.APPWIDGET_PICK");
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, createWidgetOptions());
                startActivity(intent);
            }

            Toast.makeText(this, "Открывается экран выбора виджетов...", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error opening widget picker: " + e.getMessage());
            showDetailedInstructionsWithFallback();
        }
    }
    private void showDetailedInstructionsWithFallback() {
        String instructions =
                "📱 Как добавить виджет:\n\n" +
                        "1. Нажмите кнопку HOME чтобы выйти на главный экран\n" +
                        "2. Долгое нажатие (2-3 секунды) на пустом месте\n" +
                        "3. Выберите 'Виджеты' или 'Widgets'\n" +
                        "4. Найдите 'Мой Виджет'\n" +
                        "5. Перетащите его на экран\n\n" +
                        "Совет: Ищите в списке виджетов надпись '" + getString(R.string.widget_name) + "'";

        new android.app.AlertDialog.Builder(this)
                .setTitle("Добавление виджета")
                .setMessage(instructions)
                .setPositiveButton("Понятно", null)
                .setNeutralButton("Открыть домашний экран", (dialog, which) -> {
                    // Пытаемся открыть домашний экран
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);
                })
                .show();
    }
    private Bundle createWidgetOptions() {
        Bundle options = new Bundle();
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 40);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 250);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 100);
        return options;
    }
    private void checkWidgetStatus() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this, AppWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        if (appWidgetIds.length > 0) {
            String status = " Виджеты активны: " + appWidgetIds.length + " шт.\n";
            status += " ID: ";
            for (int i = 0; i < appWidgetIds.length; i++) {
                status += appWidgetIds[i];
                if (i < appWidgetIds.length - 1) {
                    status += ", ";
                }
            }
            widgetStatusText.setText(status);
            widgetStatusText.setBackgroundColor(getColor(android.R.color.holo_green_light));
        } else {
            widgetStatusText.setText("Виджеты не активны\nДобавьте виджет на домашний экран");
            widgetStatusText.setBackgroundColor(getColor(android.R.color.holo_red_light));
        }
    }

    private void updateAllWidgets() {
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            ComponentName widgetComponent = new ComponentName(this, AppWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

            if (appWidgetIds.length > 0) {
                Intent updateIntent = new Intent(this, AppWidget.class);
                updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                sendBroadcast(updateIntent);

                Toast.makeText(this, "🔄 Виджеты обновлены! Количество: " + appWidgetIds.length, Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Manual update triggered for " + appWidgetIds.length + " widgets");

                checkWidgetStatus();
            } else {
                Toast.makeText(this, "❌ Нет активных виджетов для обновления", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error updating widgets: " + e.getMessage());
            Toast.makeText(this, "⚠️ Ошибка обновления виджетов", Toast.LENGTH_SHORT).show();
        }
    }
    private void showDetailedInstructions() {
        String instructions =
                "📋 Подробная инструкция по добавлению виджета:\n\n" +

                        " Для большинства лаунчеров:\n" +
                        "• Долгое нажатие на домашнем экране\n" +
                        "• Выберите 'Виджеты'\n" +
                        "• Найдите '" + getString(R.string.widget_name) + "'\n" +
                        "• Перетащите на экран\n\n" +

                        " Для некоторых лаунчеров:\n" +
                        "• Откройте меню приложений\n" +
                        "• Найдите вкладку 'Виджеты'\n" +
                        "• Перетащите '" + getString(R.string.widget_name) + "' на экран\n\n" +

                        " Если не нашли:\n" +
                        "• Убедитесь что приложение установлено\n" +
                        "• Перезагрузите устройство\n" +
                        "• Проверьте настройки лаунчера";

        new android.app.AlertDialog.Builder(this)
                .setTitle(" Инструкция по добавлению виджета")
                .setMessage(instructions)
                .setPositiveButton(" Понятно", null)
                .setNeutralButton(" Открыть домашний экран", (dialog, which) -> {
                    openHomeScreen();
                })
                .show();
    }
    private void openHomeScreen() {
        try {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);

            Toast.makeText(this, "Перейдите к домашнему экрану и добавьте виджет", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось открыть домашний экран", Toast.LENGTH_SHORT).show();
        }
    }
    private void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось открыть настройки", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkWidgetStatus();
        Log.d(LOG_TAG, "MainActivity resumed");
    }
    public void onLogWidgetInfo(View view) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this, AppWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        String logInfo = " Widget Information:\n";
        logInfo += "Total widgets: " + appWidgetIds.length + "\n";
        for (int id : appWidgetIds) {
            logInfo += "Widget ID: " + id + "\n";
        }

        Log.d(LOG_TAG, logInfo);
        Toast.makeText(this, "📝 Информация записана в логи", Toast.LENGTH_SHORT).show();

        // Показываем также в Toast для удобства
        if (appWidgetIds.length > 0) {
            Toast.makeText(this, "Найдено виджетов: " + appWidgetIds.length, Toast.LENGTH_SHORT).show();
        }
    }
}