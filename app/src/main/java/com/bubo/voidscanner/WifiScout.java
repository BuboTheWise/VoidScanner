package com.bubo.voidscanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.List;

public class WifiScout {
    private Context context;
    private List<String> scanResults;

    public WifiScout(Context context) {
        this.context = context;
        this.scanResults = new ArrayList<>();
    }

    public List<String> scan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) 
                    != PackageManager.PERMISSION_GRANTED) {
                scanResults.add("ERROR: NEARBY_WIFI_DEVICES permission not granted");
                return scanResults;
            }
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            scanResults.add("ERROR: ACCESS_FINE_LOCATION permission not granted");
            return scanResults;
        }

        scanResults.add("WiFi: Scanning network...");
        scanResults.add("WiFi: Waiting for scan results...");

        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) {
            scanResults.add("ERROR: WifiManager unavailable");
            return scanResults;
        }

        if (!wifiManager.isWifiEnabled()) {
            scanResults.add("ERROR: WiFi is disabled");
            return scanResults;
        }

        boolean success = wifiManager.startScan();

        if (success) {
            scanResults.add("WiFi: Scan initiated successfully");
            scanResults.add("WiFi: Waiting up to 10 seconds for results...");
        } else {
            scanResults.add("ERROR: Failed to initiate WiFi scan");
            return scanResults;
        }

        // Poll for actual scan results (up to ~8s)
        List<ScanResult> wifiScanResults = null;
        for (int i = 0; i < 8; i++) {
            wifiScanResults = wifiManager.getScanResults();
            if (wifiScanResults != null && !wifiScanResults.isEmpty()) break;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                break;
            }
        }

        if (wifiScanResults == null || wifiScanResults.isEmpty()) {
            scanResults.add("No WiFi networks found");
            return scanResults;
        }

        for (ScanResult sr : wifiScanResults) {
            String ssid = sr.SSID;
            if (ssid == null || ssid.isEmpty()) ssid = "<hidden>";
            int strength = Math.max(0, Math.min(100, sr.level + 100)); // RSSI -100..0 dBm -> 0..100 scale
            String oui = (sr.BSSID != null && sr.BSSID.length() >= 8) ? sr.BSSID.substring(0, 8).toUpperCase() : "UNKNOWN";
            scanResults.add(String.format("Found WiFi network %s (Signal: %d/100, OUI: %s)", ssid, strength, oui));
        }

        return scanResults;
    }

    public List<String> getScanResults() {
        return scanResults;
    }
}
