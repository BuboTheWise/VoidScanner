package com.bubo.voidscanner.entities;

import java.util.Random;

public class Entity {
    private final String name;
    private final Rarity rarity;
    private final String flavorText;
    private final String properties;

    public enum Rarity {
        COMMON,
        RARE,
        ELITE,
        MYTHIC
    }

    public Entity(String name, Rarity rarity, String flavorText, String properties) {
        this.name = name;
        this.rarity = rarity;
        this.flavorText = flavorText;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s\n  %s\n  %s",
                name,
                rarity,
                properties,
                flavorText,
                "Stats: " + calculateStatString(rarity));
    }

    private String calculateStatString(Rarity rarity) {
        switch (rarity) {
            case COMMON:
                return "Strength: 1, Magic: 2, Resistance: 2";
            case RARE:
                return "Strength: 3, Magic: 4, Resistance: 3";
            case ELITE:
                return "Strength: 6, Magic: 7, Resistance: 6";
            case MYTHIC:
                return "Strength: 9, Magic: 10, Resistance: 9";
            default:
                return "Strength: 1, Magic: 1, Resistance: 1";
        }
    }

    public String toJSON() {
        return String.format("{\"name\":\"%s\",\"rarity\":\"%s\",\"flavor\":\"%s\",\"properties\":\"%s\"}",
                name, rarity.toString().toLowerCase(), flavorText, properties);
    }
}