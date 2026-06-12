package com.bubo.voidscanner;

import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class EntityGenerator {
    private static final String TAG = "EntityGenerator";
    
    // Vendor mapping for OUI bias (11 vendors)
    private static final Map<String, String> VENDOR_MAP = new HashMap<>();
    private static final Set<String> KNOWN_OUIS = new HashSet<>();
    
    static {
        // Philips Hue
        VENDOR_MAP.put("00:17:88", "Philips Hue");
        VENDOR_MAP.put("00:22:43", "Philips Hue");
        VENDOR_MAP.put("00:50:F2", "Philips Hue");
        
        // Arlo
        VENDOR_MAP.put("00:1A:A0", "Arlo");
        VENDOR_MAP.put("34:1A:48", "Arlo");
        VENDOR_MAP.put("4C:C7:2D", "Arlo");
        
        // Nest
        VENDOR_MAP.put("00:26:F2", "Nest");
        VENDOR_MAP.put("00:50:56", "Nest");
        VENDOR_MAP.put("00:80:E1", "Nest");
        
        // Ecobee
        VENDOR_MAP.put("A0:23:36", "Ecobee");
        VENDOR_MAP.put("00:1A:75", "Ecobee");
        VENDOR_MAP.put("00:18:4D", "Ecobee");
        
        // TP-Link
        VENDOR_MAP.put("00:13:72", "TP-Link");
        VENDOR_MAP.put("44:DA:30", "TP-Link");
        VENDOR_MAP.put("20:CF:30", "TP-Link");
        VENDOR_MAP.put("00:18:F3", "TP-Link");
        
        // Ring
        VENDOR_MAP.put("A0:20:A6", "Ring");
        VENDOR_MAP.put("90:B1:1C", "Ring");
        VENDOR_MAP.put("B4:75:0E", "Ring");
        
        // Belkin
        VENDOR_MAP.put("08:37:3D", "Belkin");
        VENDOR_MAP.put("24:FD:52", "Belkin");
        VENDOR_MAP.put("68:7F:74", "Belkin");
        
        // Apple
        VENDOR_MAP.put("00:0A:95", "Apple");
        VENDOR_MAP.put("00:11:24", "Apple");
        VENDOR_MAP.put("00:1D:FA", "Apple");
        VENDOR_MAP.put("5C:83:8F", "Apple");
        
        // Samsung
        VENDOR_MAP.put("00:1B:44", "Samsung");
        VENDOR_MAP.put("00:24:FD", "Samsung");
        VENDOR_MAP.put("00:F4:2D", "Samsung");
        
        // Others vendor OUIs (for general devices)
        VENDOR_MAP.put("00:00:00", "Generic Device");
        VENDOR_MAP.put("00:00:01", "Generic Device");
        VENDOR_MAP.put("00:00:02", "Generic Device");
        
        // Add all OUIs to KNOWN_OUIS set for lookup
        for (String oui : VENDOR_MAP.keySet()) {
            KNOWN_OUIS.add(oui);
        }
    }

    /**
     * ScanFeatures - Container for sensor data used in entity generation
     */
    public static class ScanFeatures {
        public int humanDensity;            // Number of Bluetooth devices detected
        public int proximity;               // Strong (high signal) Bluetooth devices
        public int iotPresence;             // Number of known IoT OUIs detected
        public List<String> unknownOuis;    // List of unknown OUIs for entropy
        public double wifiRssiAvg;          // Average WiFi RSSI 
        public int signalChaos;             // Combined signal strength variance
        public int techLevel;               // General technological complexity estimation
        public double movement;             // Movement indicator
        public int environment;             // Environmental entropy score
        public String direction;            // Directionality (OUTDOOR/INDOOR)
        public int beaconCount;             // Number of beacon-like devices
        
        public ScanFeatures() {
            unknownOuis = new ArrayList<>();
        }
    }

    /**
     * Generate entities from scan data with deterministic OUI bias mapping and signal strength-based rarity
     * @param scanId Unique identifier for this scan
     * @param features Sensor data to process
     * @return List of discovered entities
     */
    public static List<DiscoveredEntity> generateFromScan(String scanId, ScanFeatures features) {
        // Validate inputs
        if (scanId == null || features == null) {
            return Collections.emptyList();
        }
        
        List<DiscoveredEntity> entities = new ArrayList<>();
        
        try {
            Random random = new Random();
            int timestamp = (int) (System.currentTimeMillis() / 1000);
            
            // Create one main entity for the scan based on key features
            String entityHash = generateEntityHash(scanId, features);
            String baseType = "Scanning Device";
                        
            // Determine overall tech sophistication based on signal chaos and tech level
            Rarity rarity = determineRarityBySignal(features, random);
            
            // Get vendor from known OUIs or create generic
            String vendorName = getVendorFromOuis(features);
            
            // Generate name, flavor text and properties
            String entityName = generateEntityName(rarity, vendorName, random);
            String flavorText = generateFlavorText(rarity, features);
            int powerLevel = (int) Math.pow(1.5, features.techLevel / 20.0) * 100;
            
            // Build influencing OUIs list
            List<String> influencingOuis = new ArrayList<>();
            if (!features.unknownOuis.isEmpty()) {
                influencingOuis.addAll(features.unknownOuis);
            }
            
            // If we have known OUIs, we might include them for bias
            if (influencingOuis.isEmpty() && features.iotPresence > 0) {
                // Generate some mock OUIs for demonstration
                for (int i = 0; i < Math.min(features.iotPresence, 3); i++) {
                    String mockOui = generateRandomOui(random);
                    influencingOuis.add(mockOui);
                }
            }
            
            DiscoveredEntity entity = new DiscoveredEntity(
                entityHash,
                entityName,
                baseType,
                rarity,
                flavorText,
                powerLevel,
                timestamp,
                influencingOuis,
                scanId
            );
            
            entities.add(entity);
            
            // Add a few random additional entities based on the data
            int additionalCount = Math.min(features.iotPresence, 5);  
            for (int i = 0; i < additionalCount; i++) {
                Rarity additionalRarity = Rarity.COMMON;
                // Adjust rarity based on signal strength
                if (features.signalChaos > 30) {
                    additionalRarity = Rarity.RARE;
                }
                if (features.signalChaos > 60) {
                    additionalRarity = Rarity.ELITE;
                }
                if (features.signalChaos > 90) {
                    additionalRarity = Rarity.MYTHIC;
                }
                
                // Add vendor bias for these additional entities
                String additionalVendor = vendorName;
                if (features.unknownOuis.size() > i) {
                    additionalVendor = getVendorFromOui(features.unknownOuis.get(i));
                }
                
                String additionalName = generateEntityName(additionalRarity, additionalVendor, random);
                String additionalFlavorText = generateFlavorText(additionalRarity, features);
                int additionalPowerLevel = (int) (powerLevel * 0.5 + random.nextInt(30));
                
                DiscoveredEntity additionalEntity = new DiscoveredEntity(
                    generateEntityHash(scanId + "_add" + i, features),
                    additionalName,
                    baseType,
                    additionalRarity,
                    additionalFlavorText,
                    additionalPowerLevel,
                    timestamp + i,
                    influencingOuis,
                    scanId
                );
                
                entities.add(additionalEntity);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating entities", e);
        }
        
        return entities;
    }
    
    // Create a deterministic hash based on scan features for entity consistency
    private static String generateEntityHash(String scanId, ScanFeatures features) {
        int hash = Objects.hash(scanId, features.humanDensity, features.proximity, 
                              features.iotPresence, features.techLevel, features.signalChaos);
        return Integer.toString(Math.abs(hash));
    }
    
    // Determine entity rarity based on signal strength and environmental factors
    private static Rarity determineRarityBySignal(ScanFeatures features, Random random) {
        // If we have a lot of strong signals or high tech levels, entities are more rare
        double signalStrength = (features.proximity + features.humanDensity + 
                                (features.wifiRssiAvg / 10.0)) / 3.0;
        
        if (signalStrength > 45) {
            return Rarity.MYTHIC;
        } else if (signalStrength > 30) {
            return Rarity.ELITE;
        } else if (signalStrength > 15) {
            return Rarity.RARE;
        } else {
            return Rarity.COMMON;
        }
    }
    
    // Get vendor name from OUIs or fall back to others
    private static String getVendorFromOuis(ScanFeatures features) {
        if (!features.unknownOuis.isEmpty()) {
            for (String oui : features.unknownOuis) {
                String vendor = getVendorFromOui(oui);
                if (vendor != null && !vendor.equals("Generic Device")) {
                    return vendor;
                }
            }
        }
        return "Others";
    }
    
    // Get specific vendor from OUI
    public static String getVendorFromOui(String oui) {
        if (VENDOR_MAP.containsKey(oui)) {
            return VENDOR_MAP.get(oui);
        } else {
            return "Generic Device";
        }
    }
    
    // Check if an OUI is known
    public static boolean isKnownOui(String oui) {
        return KNOWN_OUIS.contains(oui);
    }
    
    // Generate random OUI for mock data
    private static String generateRandomOui(Random random) {
        StringBuilder oui = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) oui.append(":");
            oui.append(String.format("%02X", random.nextInt(256)));
        }
        return oui.toString();
    }
    
    // Generate appropriate entity name based on rarity and vendor information
    private static String generateEntityName(Rarity rarity, String vendorName, Random random) {
        String[] commonNames = {"Shadow", "Echo", "Resonance", "Wisp", "Phantom", "Spirit", 
                               "Vessel", "Trace", "Whisper"};
        String[] rareNames = {"Lord", "Master", "Guardian", "Overseer", "Sentinel", 
                             "Sovereign", "Emperor", "Deity", "Divine"};
        String[] eliteNames = {"Knight", "Adept", "Mystic", "Oracle", "Sage", 
                              "Luminary", "Ascendant", "Avatar", "Radiant"};
        String[] mythicNames = {"Architect", "Creator", "Cosmos", "Universe", "Nexus", 
                               "Eternal", "Primordial", "Supreme", "Omnipotent"};
        
        String[] names;
        switch (rarity) {
            case MYTHIC:
                names = mythicNames;
                break;
            case ELITE:
                names = eliteNames;
                break;
            case RARE:
                names = rareNames;
                break;
            default: // COMMON
                names = commonNames;
                break;
        }
        
        String baseName = names[random.nextInt(names.length)];
        if (vendorName == null || vendorName.equals("Others")) {
            return baseName;
        } else {
            return String.format("%s %s", vendorName, baseName);
        }
    }
    
    // Generate appropriate flavor text
    private static String generateFlavorText(Rarity rarity, ScanFeatures features) {
        Map<Rarity, String[]> flavorMap = new HashMap<>();
        
        flavorMap.put(Rarity.COMMON, new String[]{
            "A faint echo of technology whispers in the void.",
            "A distant hum resonates through the noise.",
            "Technology's subtle trace lingers here.",
            "A whisper of past presence remains."
        });
        
        flavorMap.put(Rarity.RARE, new String[]{
            "An ancient echo of advanced systems echoes around you.",
            "A rare resonance from sophisticated devices permeates the air.",
            "The technology has evolved beyond normal comprehension.",
            "A glimpse into a realm of enhanced presence."
        });
        
        flavorMap.put(Rarity.ELITE, new String[]{
            "Powerful technological forces gather here with intense focus.",
            "Ancient wisdom echoes in this advanced domain.",
            "Mystical energies align with cutting-edge innovations.",
            "The boundary between reality and technology becomes blurred."
        });
        
        flavorMap.put(Rarity.MYTHIC, new String[]{
            "Divine technology pulsates with cosmic power here.",
            "The very fabric of space-time bends under the force of this realm.",
            "Cosmic consciousness forms in a convergence of supreme systems.",
            "The presence of divine engineering echoes through all dimensions."
        });
        
        String[] texts = flavorMap.get(rarity);
        return texts[new Random().nextInt(texts.length)];
    }
}