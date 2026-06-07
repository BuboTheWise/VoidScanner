package com.bubo.voidscanner.entities;

public class Entity {
    private final String name;
    private final Rarity rarity;
    private final String flavorText;
    private final String properties;

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

    public String getFlavorText() {
        return flavorText;
    }

    public String getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s\n  %s\n  Stats: %s",
                name,
                rarity,
                properties,
                flavorText,
                getStatString());
    }

    private String getStatString() {
        switch (rarity) {
            case COMMON:
                return "1, 2, 2";
            case RARE:
                return "3, 4, 3";
            case ELITE:
                return "6, 7, 6";
            case MYTHIC:
                return "9, 10, 9";
            default:
                return "0, 0, 0";
        }
    }

    public String toJSON() {
        return String.format("{\"name\":\"%s\",\"rarity\":\"%s\",\"flavor\":\"%s\",\"properties\":\"%s\"}",
                name, rarity.toString().toLowerCase(), flavorText, properties);
    }
}