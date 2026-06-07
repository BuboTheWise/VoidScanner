package com.bubo.voidscanner;

import java.util.List;

/**
 * DiscoveredEntity - Represents a discovered Void Echo entity.
 * Each entity has a stable hash for network-wide identification.
 * 
 * @version 1.2.0
 */
public class DiscoveredEntity {
    private final String entityHash;       // SHA-256 hex (stable ID)
    private final String name;             // Entity name with modifier
    private final String baseType;         // Base entity type (e.g., "Watcher")
    private final Rarity rarity;           // Entity rarity tier
    private final String flavorText;       // Flavor description
    private final int powerLevel;          // 0-100 power
    private final long seed;               // Generation seed
    private final List<String> influencingOuis;  // OUIs that influenced this entity
    private final String scanId;           // Associated scan ID
    
    public enum Rarity {
        COMMON,
        UNCOMMON,
        RARE,
        ANOMALOUS
    }
    
    public DiscoveredEntity(String entityHash, String name, String baseType,
                           Rarity rarity, String flavorText, int powerLevel,
                           long seed, List<String> influencingOuis, String scanId) {
        this.entityHash = entityHash;
        this.name = name;
        this.baseType = baseType;
        this.rarity = rarity;
        this.flavorText = flavorText;
        this.powerLevel = powerLevel;
        this.seed = seed;
        this.influencingOuis = influencingOuis;
        this.scanId = scanId;
    }
    
    public String getEntityHash() {
        return entityHash;
    }
    
    public String getName() {
        return name;
    }
    
    public String getBaseType() {
        return baseType;
    }
    
    public Rarity getRarity() {
        return rarity;
    }
    
    public String getFlavorText() {
        return flavorText;
    }
    
    public int getPowerLevel() {
        return powerLevel;
    }
    
    public long getSeed() {
        return seed;
    }
    
    public List<String> getInfluencingOuis() {
        return influencingOuis;
    }
    
    public String getScanId() {
        return scanId;
    }
    
    @Override
    public String toString() {
        return String.format("Entity{hash='%s', name='%s', type='%s', rarity=%s, power=%d, OUIs=%s}",
                           entityHash, name, baseType, rarity, powerLevel, influencingOuis);
    }
}
