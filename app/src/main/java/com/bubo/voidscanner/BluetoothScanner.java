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
import android.content.IntentFilter;
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

        final List<BluetoothDevice> found = new ArrayList<>();
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) found.add(device);
                }
            }
        };

        context.getApplicationContext().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        // Wait up to 12 seconds for discovered devices
        for (int i = 0; i < 12 && found.isEmpty(); i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                break;
            }
        }

        try {
            bluetoothAdapter.cancelDiscovery();
        } catch (Exception ignored) {
        }
        context.getApplicationContext().unregisterReceiver(receiver);

        if (found.isEmpty()) {
            scanResults.add("No Bluetooth devices found");
            return scanResults;
        }

        for (BluetoothDevice device : found) {
            String name = device.getName();
            if (name == null || name.isEmpty()) name = "Unknown Device";
            String mac = device.getAddress();
            String oui = (mac != null && mac.length() >= 8) ? mac.substring(0, 8).toUpperCase() : "UNKNOWN";
            scanResults.add(String.format("Found Bluetooth device %s (Signal: 50/100, OUI: %s)", name, oui));
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
