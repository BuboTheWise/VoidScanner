package com.bubo.voidscanner;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.List;

public class BluetoothScanner {
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> scanResults;

    public BluetoothScanner(Context context) {
        this.context = context;
        this.scanResults = new ArrayList<>();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public ArrayList<String> scan() {
        if (bluetoothAdapter == null) {
            scanResults.add("ERROR: Bluetooth not supported on this device");
            return scanResults;
        }

        if (!bluetoothAdapter.isEnabled()) {
            scanResults.add("ERROR: Bluetooth is disabled");
            return scanResults;
        }

        // Check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) 
                    != PackageManager.PERMISSION_GRANTED) {
                scanResults.add("ERROR: BLUETOOTH_SCAN permission not granted (Android 12+)");
                return scanResults;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                scanResults.add("ERROR: BLUETOOTH_CONNECT permission not granted (Android 12+)");
                return scanResults;
            }
        }

        // Legacy permissions for Android < 12
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            scanResults.add("ERROR: ACCESS_FINE_LOCATION permission not granted");
            return scanResults;
        }

        scanResults.add("Bluetooth: Starting discovery...");

        // Use classic discovery
        boolean isInDiscovery = bluetoothAdapter.startDiscovery();

        if (isInDiscovery) {
            scanResults.add("Bluetooth: Discovery started");
            scanResults.add("Bluetooth: Waiting up to 12 seconds for discovered devices...");
        } else {
            scanResults.add("ERROR: Failed to start Bluetooth discovery");
        }

        return scanResults;
    }

    public ArrayList<String> getScanResults() {
        return scanResults;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
}