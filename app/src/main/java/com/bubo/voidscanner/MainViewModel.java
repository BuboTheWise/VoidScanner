package com.bubo.voidscanner;

import android.content.Context;
import android.view.ContextThemeWrapper;
import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import com.bubo.voidscanner.entities.*;
import com.bubo.voidscanner.R;

public class MainActivity extends AppCompatActivity {
    private TextView statusTextView;
    private TextView resultsTextView;
    private List<String> currentScanResults = new ArrayList<>();
    private SensorManager sensorManager;
    private WifiManager wifiManager;

    private void handleStartScan() {
        resultsTextView.setText("Scanning for entities...");

        // Prepare sensor data
        Map<String, Object> sensorData = new HashMap<>();
        sensorData.put("available_sensors", "WiFi + Bluetooth");
        sensorData.put("signal_count", currentScanResults.size());

        // Generate entities from current device signatures
        List<Entity> entities = EntityGenerator.generateFromScan(
            currentScanResults,
            sensorData
        );

        // Display discovered entities
        displayEntities(entities);

        // Start scanning
        isScanning = true;
        statusTextView.setText("Scanning...");
    }

    private void displayEntities(List<Entity> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        Context context = new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light);
        resultsTextView.append("\n\n=== ENTITY DISCOVERY ===\n");

        for (Entity entity : entities) {
            String entityDisplay = String.format(
                "\n【%s】%s\n  • %s\n  • %s",
                entity.getRarity(),
                entity.getName(),
                entity.getFlavorText(),
                entity.getProperties()
            );
            resultsTextView.append(entityDisplay);
        }
    }
}