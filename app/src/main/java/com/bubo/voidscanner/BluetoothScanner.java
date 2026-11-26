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

        // Use classic discovery
        boolean isInDiscovery = bluetoothAdapter.startDiscovery();
        if (!isInDiscovery) {
            scanResults.add("ERROR: Failed to start Bluetooth discovery");
            return scanResults;
        }
        scanResults.add("Bluetooth: Starting discovery...");

        final List<BluetoothDevice> found = new ArrayList<>();

        // Listen for ACTION_FOUND broadcasts while discovery is running
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BLUETOOTH_DEVICE);
                    String name = device.getName();
                    String rssiString = intent.getStringExtra(SIGNAL_RSSI);
                    String ouiString = intent.getStringExtra(OUI_STRING);

                    int signal = 0;
                    if (rssiString != null) {
                        try {
                            signal = Integer.parseInt(rssiString.trim().split("/")[0].trim());
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Invalid signal: " + rssiString);
                        }
                    }

                    String oui = "";
                    if (ouiString != null) {
                        // Extract OUI from format like "XX:XX:XX)" or clean it up
                        String cleanedOui = ouiString.replace(")", "").trim();
                        try {
                            oui = cleanedOui.split(":")[0].substring(1);  // Remove leading dash if present
                        } catch (Exception e) {
                            Log.e(TAG, "Invalid OUI format: " + ouiString);
                        }
                    }

                    String scanLine = String.format("Found Bluetooth device %s (Signal: %d/100, OUI: %s)", name, signal, oui);
                    scanResults.add(scanLine);
                } else if (intent.getAction().equals(ACTION_DISCOVERY_FINISHED)) {
                    Log.d(TAG, "Discovery finished");
                } else if (intent.getAction().equals(ACTION_SCAN_FAILED)) {
                    Log.e(TAG, "Scan failed with code: " + intent.getIntExtra(RESULT_CODE, 0));
                    scanResults.add("ERROR: Bluetooth scan failed with code: " + intent.getIntExtra(RESULT_CODE, 0));
                } else if (intent.getAction().equals(ACTION_DISCOVERY_TIMEOUT)) {
                    Log.d(TAG, "Discovery timeout");
                }
            }
        };

        try {
            context.registerReceiver(receiver, new IntentFilter(ACTION_FOUND));
            context.registerReceiver(receiver, new IntentFilter(ACTION_DISCOVERY_FINISHED));
            context.registerReceiver(receiver, new IntentFilter(ACTION_SCAN_FAILED));
            context.registerReceiver(receiver, new IntentFilter(ACTION_DISCOVERY_TIMEOUT));

            return scanResults;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid receiver");
        } catch (Exception e) {
            Log.e(TAG, "Error generating entities", e);
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