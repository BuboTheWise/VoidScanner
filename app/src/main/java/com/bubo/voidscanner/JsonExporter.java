package com.bubo.voidscanner;

import android.content.Context;
import android.os.Environment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JsonExporter {

    private final Gson gson;
    private final SimpleDateFormat dateFormat;

    public JsonExporter(Context context) {
        // Configure Gson for pretty printing
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    }

    public boolean exportData(Map<String, Object> data, String filename, boolean debugMode) {
        try {
            // Create JSON object from data
            Map<String, Object> root = new HashMap<>();

            // Add timestamp
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("created_at", dateFormat.format(new Date()));
            metadata.put("device_model", "Pixel 5");
            metadata.put("android_version", "14 (API 34)");
            metadata.put("app_version", "1.0");
            metadata.put("debug_mode", debugMode);
            root.put("metadata", metadata);

            // Add collected data
            if (debugMode) {
                root.put("raw_data", data);
            } else {
                // Anonymize data for release
                root.put("anonymized_data", anonymizeData(data));
            }

            // Add scan results
            Map<String, Integer> scans = new HashMap<>();
            // This would be populated from actual scan data
            scans.put("wifi_scans", 0);
            scans.put("bluetooth_scans", 0);
            scans.put("location_scans", 0);
            root.put("scans", scans);

            // Write to storage
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "VeilScanner");
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                return false;
            }

            File outputFile = new File(storageDir, filename);
            try (FileWriter writer = new FileWriter(outputFile)) {
                gson.toJson(root, writer);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Map<String, Object> anonymizeData(Map<String, Object> rawData) {
        Map<String, Object> anonymized = new HashMap<>();

        // WiFi anonymization
        if (rawData.containsKey("wifi_networks")) {
            Map<String, String> wifiObj = new HashMap<>();
            wifiObj.put("ssid_anonymized", "Network_Hash_XYZ123");
            wifiObj.put("bssid_anonymized", "AA:BB:CC:DD:EE:FF");
            wifiObj.put("RSSI", "Signal_Strength_Unknown");
            anonymized.put("wifi_networks", wifiObj);
        }

        // Bluetooth anonymization
        if (rawData.containsKey("bluetooth_devices")) {
            Map<String, String> btObj = new HashMap<>();
            btObj.put("device_name_anonymized", "Device_AbC123");
            btObj.put("device_address_anonymized", "11:22:33:44:55:66");
            btObj.put("rssi", "Signal_Strength_Unknown");
            anonymized.put("bluetooth_devices", btObj);
        }

        // Location anonymization
        if (rawData.containsKey("latitude")) {
            anonymized.put("latitude_confidence", "Medium");
            anonymized.put("longitude_confidence", "Medium");
            // Don't store actual coordinates for privacy
        }

        // Sensor data (no PII in sensors)
        anonymized.put("sensor_metadata", rawData);

        return anonymized;
    }
}