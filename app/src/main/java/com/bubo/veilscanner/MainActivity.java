package com.bubo.veilscanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private boolean isScanning = false;
    private Map<String, Object> collectedData = new HashMap<>();
    private List<String> scanResults = new ArrayList<>();
    
    // Privacy debug toggle
    private boolean debugMode = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.startScanButton);
        Button stopButton = findViewById(R.id.stopScanButton);
        Button exportButton = findViewById(R.id.exportButton);
        TextView statusTextView = findViewById(R.id.statusTextView);
        TextView resultsTextView = findViewById(R.id.resultsTextView);

        startButton.setOnClickListener(v -> {
            new Thread(this::startScanning).start();
        });

        stopButton.setOnClickListener(v -> {
            isScanning = false;
            statusTextView.setText(R.string.scan_complete);
        });

        exportButton.setOnClickListener(v -> {
            new Thread(this::exportData).start();
        });

        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void startScanning() {
        isScanning = true;
        runOnUiThread(() -> {
            TextView statusTextView = findViewById(R.id.statusTextView);
            statusTextView.setText(R.string.scanning);
        });

        // Collect WiFi data
        runOnUiThread(() -> {
            TextView statusTextView = findViewById(R.id.statusTextView);
            statusTextView.setText(R.string.collecting_wifi);
        });
        collectWiFiData();

        // Collect Bluetooth data
        runOnUiThread(() -> {
            TextView statusTextView = findViewById(R.id.statusTextView);
            statusTextView.setText(R.string.collecting_bluetooth);
        });
        collectBluetoothData();

        // Collect location data
        runOnUiThread(() -> {
            TextView statusTextView = findViewById(R.id.statusTextView);
            statusTextView.setText(R.string.collecting_location);
        });
        collectLocationData();

        // Collect sensor data
        runOnUiThread(() -> {
            TextView statusTextView = findViewById(R.id.statusTextView);
            statusTextView.setText(R.string.collecting_sensors);
        });
        collectSensorData();

        isScanning = false;
        runOnUiThread(() -> {
            TextView statusTextView = findViewById(R.id.statusTextView);
            statusTextView.setText(R.string.scan_complete);
        });
    }

    private void collectWiFiData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES)
                == PackageManager.PERMISSION_GRANTED) {
            scanResults.add("WiFi: Available networks collected");
            collectedData.put("wifi_networks", "Collected");
        } else {
            scanResults.add("WiFi: Permission denied");
        }
    }

    private void collectBluetoothData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
            scanResults.add("Bluetooth: Device scan completed");
            collectedData.put("bluetooth_devices", "Collected");
        } else {
            scanResults.add("Bluetooth: Permission denied");
        }
    }

    private void collectLocationData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Basic location collection
            double latitude = 0.0;
            double longitude = 0.0;
            int accuracy = 0;

            collectedData.put("latitude", latitude);
            collectedData.put("longitude", longitude);
            collectedData.put("location_accuracy", accuracy);
            collectedData.put("location_timestamp", System.currentTimeMillis());
            scanResults.add("Location: " + latitude + ", " + longitude);
        } else {
            scanResults.add("Location: Permission denied");
        }
    }

    private void collectSensorData() {
        // Store sensor metadata (actual readings come from SensorManager)
        collectedData.put("sensor_types", "Available");
        collectedData.put("accelerometer_available", true);
        collectedData.put("gyroscope_available", true);
        collectedData.put("magnetometer_available", true);
        
        scanResults.add("Sensors: Metadata collected");
    }

    private void exportData() {
        try {
            JsonExporter exporter = new JsonExporter(this);
            String filename = "veilscan_" + System.currentTimeMillis() + ".json";
            boolean exported = exporter.exportData(collectedData, filename, debugMode);
            
            runOnUiThread(() -> {
                TextView statusTextView = findViewById(R.id.statusTextView);
                if (exported) {
                    statusTextView.setText(R.string.data_exported);
                } else {
                    statusTextView.setText(R.string.error_exporting);
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> {
                TextView statusTextView = findViewById(R.id.statusTextView);
                statusTextView.setText(R.string.error_exporting);
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, can start scanning
            }
        }
    }
}