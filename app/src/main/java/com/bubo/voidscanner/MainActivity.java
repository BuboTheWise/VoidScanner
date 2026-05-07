package com.bubo.voidscanner;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private boolean isScanning = false;
    private boolean locationPermissionGranted = false;
    private Map<String, Object> collectedData = new HashMap<>();
    private List<String> scanResults = new ArrayList<>();
    private TextView statusTextView;
    private TextView resultsTextView;

    private SensorManager sensorManager;
    private WifiManager wifiManager;

    // Constants
    private static final int REQUEST_CODE_LOCATION = 1;
    private static final int REQUEST_CODE_BT = 2;
    private static final int REQUEST_CODE_WIFI = 3;

    // Broadcast Receivers
    private BroadcastReceiver wifiReceiver;
    private BroadcastReceiver bluetoothReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        initializeUi();
        initializeBroadcastReceivers();
        checkPermissions();
    }

    private void initializeUi() {
        statusTextView = findViewById(R.id.statusTextView);
        resultsTextView = findViewById(R.id.resultsTextView);

        Button startButton = findViewById(R.id.startScanButton);
        Button stopButton = findViewById(R.id.stopScanButton);
        Button exportButton = findViewById(R.id.exportButton);

        startButton.setOnClickListener(v -> {
            if (locationPermissionGranted && validateAllPermissions()) {
                new Thread(this::startScanning).start();
            } else {
                checkPermissions();
                Toast.makeText(this, "Please grant all required permissions", Toast.LENGTH_SHORT).show();
            }
        });

        stopButton.setOnClickListener(v -> {
            isScanning = false;
            statusTextView.setText("Scan stopped");
            Toast.makeText(this, "Scan stopped", Toast.LENGTH_SHORT).show();
        });

        exportButton.setOnClickListener(v -> {
            new Thread(this::exportData).start();
        });
    }

    private void initializeBroadcastReceivers() {
        // WiFi scan results receiver
        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                    if (isScanning) {
                        handleWifiResults();
                    }
                }
            }
        };

        // Bluetooth discovery receiver
        bluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action != null) {
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                        if (device != null) {
                            String deviceInfo = String.format("Bluetooth: Found '%s' (Addr=%s, RSSI=%d dBm)",
                                    device.getName(), device.getAddress(), rssi);
                            resultsTextView.append("\n" + deviceInfo);
                        }
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        resultsTextView.append("\nBluetooth: Discovery started");
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        resultsTextView.append("\nBluetooth: Discovery completed");
                        statusTextView.setText("Scan complete");
                        isScanning = false;
                    }
                }
            }
        };
    }

    private boolean validateAllPermissions() {
        boolean valid = true;
        // Check all required permissions exist in manifest and have been granted

        return valid && locationPermissionGranted;
    }

    private void checkPermissions() {
        // Check location permission
        locationPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!locationPermissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, REQUEST_CODE_LOCATION);
            }
            return;
        }

        handlePermissionsGranted();
    }

    private void handlePermissionsGranted() {
        runOnUiThread(() -> {
            statusTextView.setText("Permissions granted - Ready to scan");
            resultsTextView.setText("Ready to scan. Tap Start to begin.\n\n" +
                    "Your device:\n" +
                    "- Android " + Build.VERSION.SDK_INT + "\n" +
                    "- WiFi: " + (wifiManager != null && wifiManager.isWifiEnabled() ? "Enabled" : "Disabled"));
        });
    }

    private void startScanning() {
        isScanning = true;

        // Reset data
        collectedData.clear();

        // Update UI
        runOnUiThread(() -> {
            statusTextView.setText("Scanning...");
            resultsTextView.setText("Starting scan...\n\n" +
                    "Checking permissions...\n" +
                    "Accessing system services...");
        });

        // Collect WiFi data
        new Thread(() -> {
            collectWifiData();
            handleWifiResults();
        }).start();

        // Collect sensor data
        collectSensorData();
    }

    private void collectWifiData() {
        if (wifiManager == null || !wifiManager.isWifiEnabled()) {
            runOnUiThread(() -> {
                resultsTextView.append("\nWiFi not enabled");
                statusTextView.setText("WiFi disabled");
            });
            return;
        }

        if (!isScanning) return;

        runOnUiThread(() -> {
            statusTextView.setText("Scanning WiFi...");
            resultsTextView.append("\nWiFi: Starting scan...");
        });

        // Start WiFi scan
        boolean success = wifiManager.startScan();

        if (success) {
            runOnUiThread(() -> resultsTextView.append("\nWiFi: Scan initiated, waiting for results..."));
        } else {
            runOnUiThread(() -> {
                resultsTextView.append("\nWiFi: Failed to start scan");
                statusTextView.setText("Scan started (WiFi error)");
            });
        }
    }

    private void handleWifiResults() {
        if (!isScanning) return;

        List<ScanResult> wifiList = wifiManager.getScanResults();

        if (wifiList == null || wifiList.isEmpty()) {
            runOnUiThread(() -> {
                resultsTextView.append("\nWiFi: No networks found");
                statusTextView.setText("Scan completed - WiFi");
            });
            return;
        }

        runOnUiThread(() -> {
            resultsTextView.append("\n\nWiFi Networks found: " + wifiList.size());
            statusTextView.setText("Scanning - " + wifiList.size() + " Wi-Fi networks");

            for (ScanResult result : wifiList) {
                String entry = String.format("  • SSID: %s, BSSID: %s, Level: %d dBm, Channel: %d",
                        result.SSID, result.BSSID, result.level, result.frequency);
                resultsTextView.append("\n" + entry);

                // Data collection
                Map<String, Object> network = new HashMap<>();
                network.put("ssid", result.SSID);
                network.put("bssid", result.BSSID);
                network.put("level", result.level);
                network.put("frequency", result.frequency);
                network.put("capabilities", result.capabilities);

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> networkList = (List<Map<String, Object>>) collectedData.get("wifi_networks");
                if (networkList == null) {
                    networkList = new ArrayList<>();
                    collectedData.put("wifi_networks", networkList);
                }
                networkList.add(network);
            }
        });
    }

    private void collectSensorData() {
        // Collect sensor info
        List<String> availableSensors = new ArrayList<>();

        if (sensorManager != null) {
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                availableSensors.add("Accelerometer");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                availableSensors.add("Gyroscope");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                availableSensors.add("Magnetometer");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
                availableSensors.add("Pressure");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
                availableSensors.add("Light");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
                availableSensors.add("Proximity");
            }
        }

        collectedData.put("available_sensors", "Found " + availableSensors.size() + " sensors");

        runOnUiThread(() -> {
            resultsTextView.append("\n\nSensors: " + availableSensors.size() + " available");
            statusTextView.setText("Sensors scanned");
        });
    }

    private void exportData() {
        try {
            JsonExporter exporter = new JsonExporter(this);
            String filename = "veilscan_" + System.currentTimeMillis() + ".json";
            boolean exported = exporter.exportData(collectedData, filename, false);

            if (exported) {
                runOnUiThread(() -> {
                    statusTextView.setText("Data exported");
                    Toast.makeText(this, "Exported to " + getFilesDir() + "/" + filename,
                            Toast.LENGTH_LONG).show();
                });
            } else {
                runOnUiThread(() -> {
                    statusTextView.setText("Export failed");
                    Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                statusTextView.setText("Export error");
                Toast.makeText(this, "Export error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver);
        }
        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_LOCATION) {
            locationPermissionGranted = grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (locationPermissionGranted) {
                handlePermissionsGranted();
            }
        }
    }
}