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

        // Check permissions
        checkPermissions();
    }

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

    private void permissionsGranted() {
        if (locationPermissionGranted && bluetoothPermissionGranted) {
            statusTextView.setText("Ready to scan");
            Log.d(TAG, "All permissions granted");
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

    private void startScan() {
        isScanning = true;
        statusTextView.setText("Scanning...");
        resultsTextView.setText("");

        // Simulate scanning process
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

            if (!entities.isEmpty()) {
                displayEntities(entities);
            } else {
                resultsTextView.append("No entities discovered. Keep scanning!\n");
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

    private void displayEntities(List<Entity> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        StringBuilder output = new StringBuilder();
        output.append("\n=== DISCOVERED ENTITIES ===\n\n");

        for (Entity entity : entities) {
            output.append(String.format("【%s】%s\n  • %s\n  • Properties: %s\n  • Stats: %s\n\n",
                    entity.getRarity(), entity.getName(), entity.getFlavorText(),
                    entity.getProperties(), entity.toString().split("Stats: ")[1]));
        }

        resultsTextView.append(output.toString());
        Toast.makeText(this, String.format("Discovered %d entities", entities.size()),
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isScanning = false;
    }
}