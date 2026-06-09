package com.bubo.voidscanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bubo.voidscanner.entities.Entity;
import com.bubo.voidscanner.entities.Rarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "VoidScanner";
    private static final int REQUEST_CODE_LOCATION = 1;
    private static final int REQUEST_CODE_BLUETOOTH = 2;

    private TextView statusTextView;
    private TextView resultsTextView;
    private boolean locationPermissionGranted = false;
    private boolean bluetoothPermissionGranted = false;
    private List<String> scanResults = new ArrayList<>();
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        resultsTextView = findViewById(R.id.resultsTextView);

        // Initial permission check and UI state readiness
        // The app requires both location and bluetooth (Android 12+) for full scanning capabilities
        checkPermissions();
        Log.i(TAG, "VoidScanner application started");
    }

    /**
     * Verify all required permissions for scanning.
     *
     * Authorization flow:
     * 1. Check ACCESS_FINE_LOCATION permission
     * 2. Request permissions if needed (rational displayed if denied)
     * 3. After permission request callback, check result again
     *    - If granted, set flag and continue
     *    - If denied, show user-facing error (no silent failures)
     * 4. For Android 12+, request BLUETOOTH_SCAN permission
     * 5. Once all permissions granted, trigger UI ready state
     *
     * This duplicate check on location permission (lines 64-71) ensures
     * that even if permission was requested and already existed, we properly
     * update our internal state and trigger the scanning readiness flow.
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Location permission required for WiFi/Bluetooth scanning",
                        Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
            return;
        }
        locationPermissionGranted = true;

        // RE-VERIFY LOCATION PERMISSION AFTER REQUEST
        // This handles the case where permission was already granted before request
        // or where user selected "Allow" in system dialog
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
            return;
        }
        locationPermissionGranted = true;

        // Request Bluetooth Scan permission if available (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_CODE_BLUETOOTH);
                return;
            }
            bluetoothPermissionGranted = true;
        } else {
            bluetoothPermissionGranted = true;
        }

        permissionsGranted();
    }

    /**
     * Callback triggered when all required permissions are granted.
     *
     * This method is invoked after both location and bluetooth permissions are resolved.
     * It sets the UI state to "Ready to scan" and logs the successful authorization.
     * Note: Actual scanning begins in onRequestPermissionsResult when both flags are true,
     * but this method marks the permission stage complete for UI feedback.
     */
    private void permissionsGranted() {
        if (locationPermissionGranted && bluetoothPermissionGranted) {
            statusTextView.setText("Ready to scan");
            Log.v(TAG, "All required permissions granted - UI ready for scanning");
        } else {
            Log.w(TAG, "Partial permissions granted, scanning disabled until all complete");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_LOCATION) {
            locationPermissionGranted = grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (locationPermissionGranted) {
                permissionsGranted();
            } else {
                Toast.makeText(this, "Location permission required for scanning", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_CODE_BLUETOOTH) {
            bluetoothPermissionGranted = grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (bluetoothPermissionGranted) {
                permissionsGranted();
            }
        }

        if (locationPermissionGranted && bluetoothPermissionGranted) {
            startScan();
        }
    }

    /**
     * Start location and device scanning process.
     *
     * Authorization state: Must have both locationPermissionGranted and bluetoothPermissionGranted flags set to true.
     * This method begins the simulated scanning workflow and tracks scan state for UI updates.
     */
    private void startScan() {
        isScanning = true;
        statusTextView.setText("Scanning...");
        resultsTextView.setText("");
        Log.d(TAG, "Scan initiated - waiting for async completion (2 second delay)");

        // Simulate scanning process
        // This uses a Handler with 2-second delay to simulate real device scanning
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            scanResults.clear();
            scanResults.add("Scanning WiFi networks...");
            scanResults.add("Scanning Bluetooth devices...");

            // Simulate some scan results
            new Random().ints(3, 1, 4).forEach(i ->
                scanResults.add(String.format("Found network #%d (Signal: %d/100)", i, 50 + new Random().nextInt(50)))
            );

            // Generate entities from scan data
            List<Entity> entities = generateEntities(scanResults);
            Log.i(TAG, String.format("Scan completed with %d entity discoveries", entities.size()));

            if (!entities.isEmpty()) {
                displayEntities(entities);
            } else {
                resultsTextView.append("No entities discovered. Keep scanning!\n");
                Log.w(TAG, "Scan completed with zero entities discovered");
            }

            statusTextView.setText("Scan complete");
            isScanning = false;
        }, 2000);
    }

    private List<Entity> generateEntities(List<String> scanResults) {
        List<Entity> entities = new ArrayList<>();

        Random random = new Random();
        int timestamp = (int) (System.currentTimeMillis() / 1000);

        for (String result : scanResults) {
            if (!result.contains("Found network")) continue;

            // Extract signal strength
            String extracted = result.substring(result.lastIndexOf("Signal:") + 8);
            Double signal = Double.parseDouble(extracted.split(" ")[0]);

            // Determine rarity based on signal strength
            Rarity rarity;
            if (signal > 85) {
                rarity = Rarity.MYTHIC;
            } else if (signal > 70) {
                rarity = Rarity.ELITE;
            } else if (signal > 50) {
                rarity = Rarity.RARE;
            } else {
                rarity = Rarity.COMMON;
            }

            // Generate entity name based on rarity
            String name = generateEntityName(rarity, random);
            String flavorText = generateFlavorText(rarity);
            String properties = generateProperties(rarity);

            Entity entity = new Entity(name, rarity, flavorText, properties);
            entities.add(entity);
        }

        return entities;
    }

    private String generateEntityName(Rarity rarity, Random random) {
        String[] names = {
            "Phantom", "Wraith", "Specter", "Spirit", "Ghost",
            "Wraith", "Hollow", "Shadow", "Invisibility"
        };

        // Higher rarity entities get cooler names
        if (rarity == Rarity.MYTHIC) {
            return names[random.nextInt(names.length / 2)] + " Lord";
        } else if (rarity == Rarity.ELITE) {
            return names[random.nextInt(names.length / 2)];
        } else if (rarity == Rarity.RARE) {
            return names[random.nextInt(names.length)] + " Spirit";
        } else {
            return names[random.nextInt(names.length)] + "";
        }
    }

    private String generateFlavorText(Rarity rarity) {
        switch (rarity) {
            case COMMON:
                return "A minor spectral presence";
            case RARE:
                return "An ethereal entity with hints of power";
            case ELITE:
                return "An ancient spirit of great significance";
            case MYTHIC:
                return "A legendary entity of cosmic proportions";
            default:
                return "An unknown being";
        }
    }

    private String generateProperties(Rarity rarity) {
        switch (rarity) {
            case COMMON:
                return "Weak aura, minimal manifestation";
            case RARE:
                return "Mystical glow, faint resonance";
            case ELITE:
                return "Ancient energy, palpable presence";
            case MYTHIC:
                return "Cosmic force, reality-altering power";
            default:
                return "Unknown";
        }
    }

    /**
     * Display discovered entities to the UI.
     *
     * Shows formatted entity cards with rarity, name, flavor text, and properties.
     * Displays a Toast notification summarizing the number of entities found.
     */
    private void displayEntities(List<Entity> entities) {
        if (entities == null || entities.isEmpty()) {
            Log.w(TAG, "Attempting to display null or empty entity list - skipping");
            return;
        }

        StringBuilder output = new StringBuilder();
        output.append("\n=== DISCOVERED ENTITIES ===\n\n");

        for (Entity entity : entities) {
            output.append(String.format("[%s]%s\n  • %s\n  • Properties: %s\n  • Stats: %s\n\n",
                    entity.getRarity(), entity.getName(), entity.getFlavorText(),
                    entity.getProperties(), entity.toString().split("Stats: ")[1]));
        }

        resultsTextView.append(output.toString());
        Toast.makeText(this, String.format("Discovered %d entities", entities.size()),
                Toast.LENGTH_LONG).show();
        Log.i(TAG, String.format("Displayed %d entities to UI", entities.size()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isScanning = false;
    }
}