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
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bubo.voidscanner.entities.Entity;
import com.bubo.voidscanner.entities.Rarity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "VoidScanner";
    private static final int REQUEST_CODE_LOCATION = 1;
    private static final int REQUEST_CODE_BLUETOOTH = 2;

    // Flip to true to use the real WifiScout/BluetoothScanner results.
    // Keep false for emulator / CI where there is no real radio.
    private static final boolean USE_REAL_SCANNERS = false;

    private TextView statusTextView;
    private TextView resultsTextView;
    private Button startScanButton;
    private Button stopScanButton;
    private Button exportButton;
    private Button exportToSheetsButton;
    private boolean locationPermissionGranted = false;
    private boolean bluetoothPermissionGranted = false;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        resultsTextView = findViewById(R.id.resultsTextView);
        startScanButton = findViewById(R.id.startScanButton);
        stopScanButton = findViewById(R.id.stopScanButton);
        exportButton = findViewById(R.id.exportButton);
        exportToSheetsButton = findViewById(R.id.exportToSheetsButton);

        // Wire listeners
        startScanButton.setOnClickListener(v -> {
            if (isScanning) {
                return;
            }
            if (!locationPermissionGranted || !bluetoothPermissionGranted) {
                Toast.makeText(this, "Permissions pending — check the permission dialog", Toast.LENGTH_SHORT).show();
                return;
            }
            startScan();
        });

        stopScanButton.setOnClickListener(v -> {
            isScanning = false;
            statusTextView.setText("Scan stopped");
            Toast.makeText(this, "Scan stopped", Toast.LENGTH_SHORT).show();
        });

        exportButton.setOnClickListener(v ->
            Toast.makeText(this, "Export: no entities to export yet", Toast.LENGTH_SHORT).show());
        exportToSheetsButton.setOnClickListener(v ->
            Toast.makeText(this, "Export to Sheets: no entities to export yet", Toast.LENGTH_SHORT).show());

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

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> new Thread(() -> {
            List<String> lines = new ArrayList<>();
            if (USE_REAL_SCANNERS) {
                lines.addAll(new WifiScout(this).scan());
                lines.addAll(new BluetoothScanner(this).scan());
                Log.i(TAG, "Real scan collected " + lines.size() + " lines");
            } else {
                // Simulated scan (default): same data as before, now in a local list
                lines.add("Scanning WiFi networks...");
                lines.add("Scanning Bluetooth devices...");
                Random random = new Random();
                for (int i = 0; i < 3; i++) {
                    String oui = String.format("%02X:%02X:%02X",
                            random.nextInt(256), random.nextInt(256), random.nextInt(256));
                    if (i == 0 && random.nextBoolean()) {
                        oui = "00:17:88"; // Philips Hue
                    } else if (i == 1 && random.nextBoolean()) {
                        oui = "00:1A:A0"; // Arlo
                    }
                    String signalStrength = String.format("%d", 50 + random.nextInt(50));
                    lines.add(String.format("Found WiFi network %s (Signal: %s/100, OUI: %s)",
                            i + 1, signalStrength, oui));
                }
                for (int i = 0; i < 2; i++) {
                    String oui = String.format("%02X:%02X:%02X",
                            random.nextInt(256), random.nextInt(256), random.nextInt(256));
                    if (i == 0 && random.nextBoolean()) {
                        oui = "00:1A:A0"; // Arlo
                    }
                    String signalStrength = String.format("%d", 60 + random.nextInt(40));
                    lines.add(String.format("Found Bluetooth device %s (Signal: %s/100, OUI: %s)",
                            i + 1, signalStrength, oui));
                }
                Log.i(TAG, "Simulated scan collected " + lines.size() + " lines");
            }

            final List<DiscoveredEntity> entities = EntityGenerator.generateFromScan(
                    String.valueOf(System.currentTimeMillis()),
                    EntityGenerator.extractScanFeatures(lines)
            );

            handler.post(() -> {
                if (!entities.isEmpty()) {
                    displayEntities(entities);
                } else {
                    resultsTextView.append("No entities discovered. Keep scanning!\n");
                }
                statusTextView.setText("Scan complete");
                isScanning = false;
            });
        }).start(), 500);
    }

    private void displayEntities(List<DiscoveredEntity> entities) {
        resultsTextView.append("Entities discovered:\n\n");

        int index = 1;
        for (DiscoveredEntity entity : entities) {
            String entityStr = String.format(
                    "%d. **%s** (%s)\n   %s\n   Power: %d\n\n",
                    index++,
                    entity.getName(),
                    entity.getRarity(),
                    entity.getFlavorText(),
                    entity.getPowerLevel()
            );
            resultsTextView.append(entityStr);
        }

        Log.d(TAG, "Discovered " + entities.size() + " entities");
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
            return names[random.nextInt(names.length / 2)] + " Knight";
        } else if (rarity == Rarity.RARE) {
            return names[random.nextInt(names.length)];
        } else {
            return names[random.nextInt(names.length)];
        }
    }

    private String generateFlavorText(Rarity rarity) {
        String[] texts = {
            "A whisper of shadow passes through.",
            "Life echoes faintly in the void.",
            "Something stirs in the distance.",
            "A faint resonance lingers here."
        };
        return texts[0];
    }

    private String generateProperties(Rarity rarity) {
        switch (rarity) {
            case MYTHIC:
                return "✨ Mythic | 🔮 Legendary | ☠ High Damage";
            case ELITE:
                return "⭐ Elite | ⚡ Enhanced | ⚔ Very High Damage";
            default:
                return "⚔ Common | 🛡 Standard Defense";
        }
    }
}