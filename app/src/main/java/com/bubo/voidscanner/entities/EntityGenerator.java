package com.bubo.voidscanner.entities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * EntityGenerator - Generates deterministic entities from scan data (compatability version).
 * 
 * @version 1.2.0
 */
public class EntityGenerator {
    
    /**
     * Feature extraction results from a scan
     */
    public static class ScanFeatures {
        public int humanDensity = 0;
        public int iotPresence = 0;
        public int signalChaos = 0;
        public int techLevel = 0;
        public int proximity = 0;
        public double movement = 0.0;
        public String direction = "UNKNOWN";
        public int environment = 0;
        public List<String> unknownOuis = new ArrayList<>();
        public double wifiRssiAvg = 0.0;
        public double signalStrengthTotal = 0.0;
        public int beaconCount = 0;
        
        public ScanFeatures() {}
    }
    
    /**
     * Generate entities from scan result
     */
    public static List<DiscoveredEntity> generateFromScan(
            String scanId, ScanFeatures features) {
        
        List<DiscoveredEntity> entities = new ArrayList<>();
        
        // Generate deterministic base scan seed
        long baseSeed = generateBaseSeed(scanId, features);
        
        // Select base entities using priority rules
        int maxEntities = Math.min(1 + (int)(Math.random() * 4), 3);
        List<String> baseTypes = selectBaseEntities(baseSeed, features, maxEntities);
        
        // Generate entities
        for (String baseType : baseTypes) {
            DiscoveredEntity entity = createEntity(baseSeed, features, baseType, scanId);
            entities.add(entity);
        }
        
        // Sort for deterministic ordering
        entities.sort(Comparator.comparing(DiscoveredEntity::getEntityHash));
        
        return entities;
    }
    
    /**
     * Generate deterministic base scan seed
     */
    private static long generateBaseSeed(String scanId, ScanFeatures features) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            String key = String.format("%s|%d|%d|%d|%d|%d|%f|%s|%d",
                    scanId,
                    features.humanDensity,
                    features.iotPresence,
                    features.signalChaos,
                    features.techLevel,
                    features.proximity,
                    features.movement,
                    features.direction,
                    features.environment);
            byte[] hash = sha.digest(key.getBytes());
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
            return new Random().nextInt(Integer.MAX_VALUE);
        }
    }
    
    /**
     * Select base entities based on priority rules
     */
    private static List<String> selectBaseEntities(long seed, ScanFeatures features, int maxCount) {
        Random random = new Random(seed);
        List<String> types = determineAvailableTypes(features, random);
        if (types.isEmpty()) {
            types.add("LUMIN_WISP");
        }
        
        Collections.shuffle(types, random);
        int count = Math.min(maxCount, types.size());
        return types.subList(0, count);
    }
    
    /**
     * Determine available entity types based on scan features
     */
    private static List<String> determineAvailableTypes(ScanFeatures features, Random random) {
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
        if (features.movement > 2.0 || features.environment > 500) {
            types.add("GEOMANTIC_SPRITE");
            types.add("LUMIN_WISP");
        }
        
        return types;
    }
    
    /**
     * Create a single entity with modifiers and hash
     */
    private static DiscoveredEntity createEntity(long baseSeed, ScanFeatures features, String baseType, String scanId) {
        Random random = new Random(baseSeed * 1000);
        
        // Generate entity hash
        String entityHash = generateEntityHash(baseSeed, baseType, features.direction, features.unknownOuis, scanId);
        
        // Apply modifiers
        String name = generateName(baseSeed, baseType, features);
        int powerLevel = random.nextInt(100);
        
        return new DiscoveredEntity(
                entityHash, name, baseType, DiscoveredEntity.Rarity.COMMON,
                "", powerLevel, baseSeed, new ArrayList<>(), scanId);
    }
    
    /**
     * Generate unique entity hash
     */
    private static String generateEntityHash(long seed, String baseType, String direction, 
            List<String> unknownOuis, String scanId) {
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
            return String.valueOf(seed);
        }
    }
    
    /**
     * Generate entity name with modifier
     */
    private static String generateName(long seed, String baseType, ScanFeatures features) {
        Random random = new Random(seed);
        String[] modifiers = {"Northern", "Southern", "Eastern", "Western", "Shadow", "Void", "Mystic", "Crimson", "Azure", "Golden", "Silver"};
        
        String[] typeSuffixes = {"", "", "", "s", "Wraith", "Spirit"};
        String suffix = typeSuffixes[random.nextInt(typeSuffixes.length)];
        
        return modifiers[random.nextInt(modifiers.length)] + baseType + suffix;
    }
    
    /**
     * Calculate movement score from accelerometer variance
     */
    public static double calculateMovement(double xVar, double yVar, double zVar) {
        return Math.sqrt(xVar*xVar + yVar*yVar + zVar*zVar);
    }
    
    /**
     * Parse Mac address to OUI
     */
    public static String getOUI(String mac) {
        if (mac == null || mac.length() < 17) return null;
        return mac.substring(0, 8);
    }
}