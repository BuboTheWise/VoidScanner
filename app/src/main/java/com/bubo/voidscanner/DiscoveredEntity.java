package com.bubo.voidscanner;

import com.bubo.voidscanner.entities.Rarity;
import java.util.List;
import java.util.ArrayList;

/**
 * DiscoveredEntity - Represents an entity discovered during a scan.
 * This class is used by the EntityGenerator to create deterministic entities.
 * 
 * @version 1.2.0
 */
public class DiscoveredEntity {
    private final String entityHash;
    private final String name;
    private final String baseType;
    private final Rarity rarity;
    private final String flavorText;
    private final int powerLevel;
    private final long seed;
    private final List<String> influencingOuis;
    private final String scanId;

    public DiscoveredEntity(String entityHash, String name, String baseType, Rarity rarity,
                           String flavorText, int powerLevel, long seed,
                           List<String> influencingOuis, String scanId) {
        this.entityHash = entityHash;
        this.name = name;
        this.baseType = baseType;
        this.rarity = rarity;
        this.flavorText = flavorText;
        this.powerLevel = powerLevel;
        this.seed = seed;
        this.influencingOuis = influencingOuis != null ? new ArrayList<>(influencingOuis) : new ArrayList<>();
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
        return new ArrayList<>(influencingOuis);
    }

    public String getScanId() {
        return scanId;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - [%s]\n%s\nPower: %d\nInfluenced by: %s",
                name, rarity, baseType, flavorText, powerLevel, String.join(", ", influencingOuis));
    }
}