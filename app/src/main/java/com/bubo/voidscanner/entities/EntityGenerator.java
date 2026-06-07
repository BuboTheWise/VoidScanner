package com.bubo.voidscanner;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * EntityGenerator - Generates deterministic entities from scan data.
 * Implements improved algorithms integrating Bluetooth/WiFi/IMU data.
 * 
 * @version 1.2.0
 */
public class EntityGenerator {
    
    /**
     * Feature extraction results from a scan
     */
    public static class ScanFeatures {
        public int humanDensity;                      // Bluetooth device count
        public int iotPresence;                       // Known IoT OUIs
        public int signalChaos;                       // WiFi count + RSSI variance
        public int techLevel;                         // WiFi standard + modern OUIs
        public int proximity;                         // Strong Bluetooth RSSI (abs)
        public double movement;                       // Accelerometer variance
        public String direction;                      // Compass heading bin
        public int environment;                       // Light level / time of day
        public List<String> unknownOuis;              // Unknown device signatures
        public double wifiRssiAvg;                    // Average WiFi RSSI
        public double signalStrengthTotal;            // Sum of all signal strengths
        public int beaconCount;                       // Bluetooth beacons count
        
        public ScanFeatures() {
            this.unknownOuis = new ArrayList<>();
        }
    }
    
    /**
     * Generate entities from scan result
     */
    public static List<DiscoveredEntity> generateFromScan(
            String scanId, ScanFeatures features) {
        
        List<DiscoveredEntity> entities = new ArrayList<>();
        
        // Step 1: Generate deterministic base scan seed
        long baseSeed = generateBaseSeed(scanId, features);
        
        // Step 2: Select base entities using priority rules
        int maxEntities = 1 + (int)(Math.random() * 4); // 1-5 entities
        List<String> baseTypes = selectBaseEntities(baseSeed, features, maxEntities);
        
        // Step 3: For each entity, apply modifiers and generate hash
        for (String baseType : baseTypes) {
            DiscoveredEntity entity = createEntity(baseSeed, features, baseType, scanId);
            entities.add(entity);
        }
        
        // Ensure deterministic ordering (sort by entityHash)
        entities.sort(Comparator.comparing(DiscoveredEntity::getEntityHash));
        
        return entities;
    }
    
    /**
     * Generate deterministic base scan seed
     */
    private static long generateBaseSeed(String scanId, ScanFeatures features) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            
            // Combine scan ID and normalized features
            String key = String.format("%s|%d|%d|%d|%d|%f|%s|%d|%f",
                    scanId,
                    features.humanDensity,
                    features.iotPresence,
                    features.signalChaos,
                    features.techLevel,
                    features.proximity,
                    features.direction,
                    features.environment,
                    features.movement);
            
            byte[] hash = sha.digest(key.getBytes());
            
            // Use first 8 bytes as seed
            long seed = ((long)hash[0] << 56) | 
               ((long)(hash[1] & 0xFF) << 48) |
               ((long)(hash[2] & 0xFF) << 40) |
               ((long)(hash[3] & 0xFF) << 32) |
               ((long)(hash[4] & 0xFF) << 24) |
               ((long)(hash[5] & 0xFF) << 16) |
               ((long)(hash[6] & 0xFF) << 8) |
               (long)(hash[7] & 0xFF);
            
            return Math.abs(seed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    /**
     * Select base entities based on priority rules
     */
    private static List<String> selectBaseEntities(
            long seed, ScanFeatures features, int maxCount) {
        
        Random random = new Random(seed);
        List<String> availableTypes = determineAvailableTypes(features, random);
        List<String> selected = new ArrayList<>();
        
        // Shuffle available types
        Collections.shuffle(availableTypes, random);
        
        // Select up to maxCount
        int count = Math.min(maxCount, availableTypes.size());
        for (int i = 0; i < count; i++) {
            selected.add(availableTypes.get(i));
        }
        
        return selected;
    }
    
    /**
     * Determine available entity types based on scan features
     */
    private static List<String> determineAvailableTypes(
            ScanFeatures features, Random random) {
        
        List<String> types = new ArrayList<>();
        
        // Priority 1 - Strong IoT Presence
        if (features.iotPresence >= 2) {
            types.add("WATCHER");
            types.add("LUMINAR");
            types.add("POWER_ELEM");
            types.add("TEMPISTRY");
            types.add("MACHINE_CHORUS");
        }
        
        // Priority 2 - Bluetooth (Human Activity)
        if (features.humanDensity >= 6 || features.proximity >= 10) {
            types.add("NETHER_SWARM");
            types.add("VOID_STALKER");
            types.add("NETHERLING");
        }
        
        // Priority 3 - WiFi
        if (features.signalChaos >= 15) {
            types.add("ECHO_SHARD");
            types.add("TECH_REVENANT");
            types.add("SIGNAL_WEAVER");
        }
        
        // Priority 4 - Environmental & Movement
        double movementScore = features.direction.equals("OUTDOOR") ? 
                features.movement * 1.5 : features.movement;
        
        if (movementScore > 2.0 || features.environment > 500) {
            types.add("GEOMANTIC_SPRITE");
            types.add("LUMIN_WISP");
        }
        
        // Fallback
        if (types.isEmpty()) {
            types.add("LUMIN_WISP");
            types.add("AETHER_DRIFTER");
        }
        
        return types;
    }
    
    /**
     * Create a single entity with modifiers and hash
     */
    private static DiscoveredEntity createEntity(
            long baseSeed, ScanFeatures features, 
            String baseType, String scanId) {
        
        Random random = new Random(baseSeed * 1000);  // Unique per entity
        
        // Generate entity hash (unique stable ID)
        String entityHash = generateEntityHash(baseSeed, baseType, 
                features.direction, features.unknownOuis, scanId);
        
        // Apply modifiers
        String name = generateName(baseSeed, baseType, features);
        Rarity rarity = calculateRarity(baseSeed, features, baseType);
        String flavorText = generateFlavorText(baseType, rarity, random);
        int powerLevel = baseType.equals("AETHER_DRIFTER") ? 
                30 + random.nextInt(40) : 40 + random.nextInt(50);
        
        // Find influencing OUIs
        List<String> influencingOuis = findInfluencingOuis(features, random);
        
        return new DiscoveredEntity(
                entityHash, name, baseType, rarity,
                flavorText, powerLevel, baseSeed,
                influencingOuis, scanId);
    }
    
    /**
     * Generate unique entity hash (SHA-256)
     */
    private static String generateEntityHash(long seed, String baseType,
            String direction, List<String> unknownOuis, String scanId) {
        
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            
            String entityKey = String.format("%d|%s|%s|%s|%s",
                    seed, baseType, direction, 
                    String.join(",", unknownOuis), scanId);
            
            byte[] hash = sha.digest(entityKey.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    /**
     * Generate entity name with modifier
     */
    private static String generateName(long seed, String baseType, ScanFeatures features) {
        Random random = new Random(seed);
        
        String[] modifiers = {"Northern", "Southern", "Eastern", 
                "Western", "Shadow", "Void", "Luminar", "Mystic",
                "Crimson", "Azure", "Golden", "Silver"};
        
        // Filter modifiers by entity type preferences
        List<String> typeModifiers = new ArrayList<>();
        if (baseType.equals("WATCHER")) {
            typeModifiers.addAll(Arrays.asList("Shadow", "Void", "Crimson", "Mystic"));
        } else if (baseType.equals("LUMINAR")) {
            typeModifiers.addAll(Arrays.asList("Luminar", "Golden", "Silver", "Light"));
        } else if (baseType.equals("NETHER_SWARM")) {
            typeModifiers.addAll(Arrays.asList("Void", "Shadow", "Crimson", "Dark"));
        } else if (baseType.equals("ECHO_SHARD")) {
            typeModifiers.addAll(Arrays.asList("Crimson", "Azure", "Mystic", "Signal"));
        }
        
        Collections.shuffle(typeModifiers, random);
        String modifier = typeModifiers.isEmpty() ? 
                modifiers[random.nextInt(modifiers.length)] : 
                typeModifiers.get(0);
        
        String[] typeSuffixes = {"", "", "", "s", "Wraith", "Spirit"};
        String suffix = typeSuffixes[random.nextInt(typeSuffixes.length)];
        
        return modifier + baseType + suffix;
    }
    
    /**
     * Calculate entity rarity
     */
    private static Rarity calculateRarity(long seed, ScanFeatures features, 
            String baseType) {
        Random random = new Random(seed);
        int roll = random.nextInt(100);
        
        // Base rarity determination
        Rarity rarity = Rarity.COMMON;
        
        // Uncommon from strong single signal
        if (features.humanDensity >= 8 || features.iotPresence >= 3) {
            rarity = Rarity.UNCOMMON;
        }
        
        // Rare from strong IoT + supporting signals
        if (features.iotPresence >= 4 && features.signalChaos >= 5) {
            rarity = Rarity.RARE;
        }
        
        // Anomalous from extreme combos
        if (roll > 90 && !features.unknownOuis.isEmpty()) {
            rarity = Rarity.ANOMALOUS;
        }
        
        // Adjust for base type power level
        if (baseType.equals("AETHER_DRIFTER") && roll > 80) {
            rarity = Rarity.RARE;
        } else if (baseType.equals("NETHER_SWARM") && roll > 85) {
            rarity = Rarity.RARE;
        }
        
        return rarity;
    }
    
    /**
     * Generate flavor text
     */
    private static String generateFlavorText(String baseType, Rarity rarity, Random random) {
        if (rarity == Rarity.ANOMALOUS) {
            String[] anomalousTexts = {
                "A terrible resonance stirs in the void.",
                "Something ancient and hungry awakens.",
                "The veil thins dangerously here.",
                "Dark whispers drift through the ether."
            };
            return anomalousTexts[random.nextInt(anomalousTexts.length)];
        }
        
        String[] commonTexts = {
            "A whisper of shadow passes through.",
            "Life echoes faintly in the void.",
            "Something stirs in the distance.",
            "A faint resonance lingers here."
        };
        
        String[] rareTexts = {
            "A luminous vision approaches.",
            "Dark energy crackles nearby.",
            "A cryptic symbol pulses slowly.",
            "The boundary between worlds thins."
        };
        
        if (rarity == Rarity.RARE) {
            return rareTexts[random.nextInt(rareTexts.length)];
        }
        
        return commonTexts[random.nextInt(commonTexts.length)];
    }
    
    /**
     * Find OUIs influencing this entity
     */
    private static List<String> findInfluencingOuis(ScanFeatures features, Random random) {
        List<String> influencing = new ArrayList<>();
        
        // Add unknown OUIs with influence
        if (features.unknownOuis.size() > 0) {
            int count = Math.min(2, features.unknownOuis.size());
            for (int i = 0; i < count; i++) {
                influencing.add(features.unknownOuis.get(
                        random.nextInt(features.unknownOuis.size())));
            }
        }
        
        return influencing;
    }
    
    /**
     * Parse Mac address to OUI
     */
    public static String getOUI(String mac) {
        if (mac == null || mac.length() < 17) return null;
        return mac.substring(0, 8);  // "00:17:88:XX"
    }
    
    /**
     * Calculate movement score from accelerometer variance
     */
    public static double calculateMovement(double xVar, double yVar, double zVar) {
        return Math.sqrt(xVar*xVar + yVar*yVar + zVar*zVar);
    }
}