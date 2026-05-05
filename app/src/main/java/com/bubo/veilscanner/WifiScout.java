package com.bubo.veilscanner;

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

        // Get scan results asynchronously via BroadcastReceiver in MainActivity
        // For now, return empty and let MainActivity handle callbacks
        scanResults.add("WiFi: Would retrieve results now (requires BroadcastReceiver)");
        
        return scanResults;
    }

    public List<String> getScanResults() {
        return scanResults;
    }
}