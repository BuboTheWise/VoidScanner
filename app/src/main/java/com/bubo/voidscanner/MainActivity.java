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
            
            // Add more realistic simulated scan results for both WiFi and Bluetooth
            scanResults.add("Scanning WiFi networks...");
            scanResults.add("Scanning Bluetooth devices...");
            
            // Simulate some real scan results with proper signal strengths and OUIs
            Random random = new Random();
            
            // Add some WiFi networks with OUIs  
            for (int i = 0; i < 3; i++) {
                String oui = String.format("%02X:%02X:%02X", 
                        random.nextInt(256), random.nextInt(256), random.nextInt(256));
                
                // Include some known OUIs for demonstration
                if (i == 0 && random.nextBoolean()) {
                    oui = "00:17:88"; // Philips Hue
                } else if (i == 1 && random.nextBoolean()) {
                    oui = "00:1A:A0"; // Arlo
                }
                
                String signalStrength = String.format("%d", 50 + random.nextInt(50));
                scanResults.add(String.format("Found WiFi network %s (Signal: %s/100, OUI: %s)", 
                        i+1, signalStrength, oui));
            }

            // Add some Bluetooth devices with OUIs
            for (int i = 0; i < 2; i++) {
                String oui = String.format("%02X:%02X:%02X", 
                        random.nextInt(256), random.nextInt(256), random.nextInt(256));
                
                // Include some known OUIs for demonstration
                if (i == 0 && random.nextBoolean()) {
                    oui = "00:1A:A0"; // Arlo
                }
                
                String signalStrength = String.format("%d", 60 + random.nextInt(40));
                scanResults.add(String.format("Found Bluetooth device %s (Signal: %s/100, OUI: %s)", 
                        i+1, signalStrength, oui));
            }

            // Generate entities using EntityGenerator with Bluetooth/WiFi/IMU integration
            List<DiscoveredEntity> entities = EntityGenerator.generateFromScan(
                    String.valueOf(System.currentTimeMillis()),
                    extractScanFeatures(scanResults)
            );

            if (!entities.isEmpty()) {
                displayEntities(entities);
            } else {
                resultsTextView.append("No entities discovered. Keep scanning!\n");
            }

            statusTextView.setText("Scan complete");
            isScanning = false;
        }, 2000);
    }

    /**
     * Extract scan features for EntityGenerator.generateFromScan()
     * Integrate Bluetooth/WiFi/IMU data metrics
     */
    private EntityGenerator.ScanFeatures extractScanFeatures(List<String> scanResults) {
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();

        Random random = new Random();

        // Bluetooth (Human density)
        int bluetoothCount = 0;
        int strongBluetooth = 0;
        for (String result : scanResults) {
            if (result.contains("Bluetooth device")) {
                bluetoothCount++;
                // Extract signal strength for strong Bluetooth detection
                String extracted = result.substring(result.lastIndexOf("Signal:") + 8);
                Double signal = Double.parseDouble(extracted.split(" ")[0].replace("/", ""));
                if (signal > 50) {
                    strongBluetooth++;
                }
                
                // Try to extract OUI from the result for vendor bias
                if (result.contains("OUI:")) {
                    String oui = result.substring(result.lastIndexOf("OUI:") + 4).trim();
                    if (EntityGenerator.isKnownOui(oui)) {
                        features.unknownOuis.add(oui);
                    }
                }
            }
        }
        features.humanDensity = bluetoothCount;
        features.proximity = strongBluetooth;

        // IoT Presence (Known OUIs from OUIDatabase)
        int iotCount = 0;
        Map<String, Integer> ouiCounts = new HashMap<>();
        for (String result : scanResults) {
            if (result.contains("Found WiFi network") || result.contains("Found Bluetooth device")) {
                // Try to extract OUI
                String oui = null;
                if (result.contains("OUI: ")) {
                    oui = result.substring(result.lastIndexOf("OUI: ") + 5).trim();
                }
                
                if (oui != null) {
                    if (EntityGenerator.isKnownOui(oui)) {
                        iotCount++;
                        ouiCounts.put(oui, ouiCounts.getOrDefault(oui, 0) + 1);
                    } else {
                        features.unknownOuis.add(oui);
                    }
                }
            }
        }
        features.iotPresence = iotCount;

        // WiFi Chaos & Tech Level
        features.wifiRssiAvg = 45 + random.nextInt(30);  // Average 45-75 dBm
        features.signalChaos = scanResults.stream().filter(r -> r.contains("Found")).mapToInt(r -> random.nextInt(20) + 10).sum();
        features.techLevel = random.nextInt(100);

        // Environmental & Movement
        features.movement = 1.5 + random.nextDouble() * 3.0;
        features.environment = random.nextInt(1000);
        features.direction = random.nextBoolean() ? "OUTDOOR" : "INDOOR";
        features.beaconCount = random.nextInt(5);

        return features;
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