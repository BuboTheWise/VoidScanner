package com.bubo.voidscanner.entities;

import java.util.*;

public class EntityGenerator {
    private static final Random RANDOM = new Random();
    private static final Map<String, Entity> OUI_BIAS_MAP = new HashMap<>();

    static {
        // Initialize known OUI -> Entity bias table from [[Requirements.md]] section 2
        OUI_BIAS_MAP.put("00:17:88", new Entity("Luminar", Entity.Rarity.RARE,
                "Philips Hue lights flicker with magical awareness.",
                "Light manipulation"));
        OUI_BIAS_MAP.put("FC:9C:98", new Entity("Watcher", Entity.Rarity.ELITE,
                "Arlo cameras capture glimpses of the invisible world.",
                "Security surveillance"));
        OUI_BIAS_MAP.put("00:0D:52", new Entity("Watcher", Entity.Rarity.ELITE,
                "Arlo cameras record strange phenomena.",
                "Motion detection"));
        OUI_BIAS_MAP.put("18:B4:30", new Entity("Tempestry", Entity.Rarity.RARE,
                "Nest thermostats whisper of shifting temperatures.",
                "Environmental sensing"));
        OUI_BIAS_MAP.put("30:6F:07", new Entity("Tempestry", Entity.Rarity.RARE,
                "Ecobee devices detect spiritual warmth.",
                "Thermal monitoring"));
        OUI_BIAS_MAP.put("00:1A:2A", new Entity("Power Elemental", Entity.Rarity.COMMON,
                "TP-Link smart plugs pulse with electrical charge.",
                "Power control"));
        OUI_BIAS_MAP.put("48:3F:DA", new Entity("Power Elemental", Entity.Rarity.COMMON,
                "TP-Link switches channel mana through wires.",
                "Device control"));
        OUI_BIAS_MAP.put("24:9E:10", new Entity("Watcher", Entity.Rarity.ELITE,
                "Ring doorbells see through the veil temporarily.",
                "Video monitoring"));
        OUI_BIAS_MAP.put("B0:CE:18", new Entity("Power Elemental", Entity.Rarity.COMMON,
                "Belkin WeMo devices channel electricity.",
                "Smart appliance control"));
        OUI_BIAS_MAP.put("00:1C:42", new Entity("Aether Drifter", Entity.Rarity.RARE,
                "Apple devices carry ether in their circuits.",
                "Wireless connectivity"));
        OUI_BIAS_MAP.put("00:1A:79", new Entity("Earthen Walker", Entity.Rarity.RARE,
                "Samsung phones vibrate with terrestrial energy.",
                "Mobile computing"));
    }

    /**
     * Generate 1-5 entities from scan data following [[Requirements.md]] section 1.3
     *
     * @param scanResult WiFi scan results
     * @param sensorData Collected sensor data from [[MainActivity]]
     * @return List of generated entities, maximum 5
     */
    public static List<Entity> generateFromScan(List<ScanResult> scanResult, Map<String, Object> sensorData) {
        List<Entity> entities = new ArrayList<>();

        if (scanResult == null || scanResult.isEmpty()) {
            return entities; // No scan data, return empty list
        }

        // Calculate entity count based on signal strength and scan diversity
        int count = Math.min(5, Math.max(1, scanResult.size() / 3));
        RANDOM.setSeed(scanResult.get(0).timestampMillis);

        for (int i = 0; i < count; i++) {
            ScanResult result = scanResult.get(RANDOM.nextInt(scanResult.size()));

            // Determine base rarity from signal strength
            int signalStrength = Math.abs(result.level);
            Rarity rarity;

            if (signalStrength < -85) {
                rarity = Rarity.MYTHIC;
            } else if (signalStrength < -75) {
                rarity = Rarity.ELITE;
            } else if (signalStrength < -65) {
                rarity = Rarity.RARE;
            } else {
                rarity = Rarity.COMMON;
            }

            // Try to bias by OUI first
            Entity biasedEntity = tryBiasByOUI(result.BSSID, sensorData);

            if (biasedEntity != null) {
                entities.add(biasedEntity);
            } else {
                entities.add(generateRandomEntity(rarity, sensorData));
            }

            // Add a second entity if possible
            if (i < count - 1 && RANDOM.nextBoolean()) {
                entities.add(generateRandomEntity(
                        RANDOM.nextBoolean() ? rarity : Rarity.COMMON,
                        sensorData));
            }
        }

        return entities;
    }

    private static Entity tryBiasByOUI(String oui, Map<String, Object> sensorData) {
        // Extract OUI from BSSID (first 8 characters)
        String oui = oui.replaceAll(":", "").substring(0, 8).toUpperCase();

        // Look for known OUI matches ignoring case
        for (Map.Entry<String, Entity> entry : OUI_BIAS_MAP.entrySet()) {
            String knownOUI = entry.getKey().replaceAll(":", "").substring(0, 8).toUpperCase();
            if (oui.equals(knownOUI)) {
                // Boost rarity based on signal strength
                Entity entity = entry.getValue();
                Map<String, Object> properties = extractSensorProperties(sensorData);
                return new Entity(
                        entity.getName(),
                        enhanceRarity(entity.getRarity(), properties),
                        entity.getFlavorText(),
                        entity.getProperties()
                );
            }
        }

        return null;
    }

    private static Rarity enhanceRarity(Rarity base, Map<String, Object> sensorData) {
        int sensorCount = 0;
        if (sensorData != null) {
            Object sensorObj = sensorData.get("available_sensors");
            if (sensorObj instanceof String && sensorObj.toString().startsWith("Found")) {
                try {
                    sensorCount = Integer.parseInt(sensorObj.toString().split(" ")[1]);
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
        }

        // Higher signal + more sensors = better rarity chance
        int chance = RANDOM.nextInt(100);
        int threshold = 20 + (sensorCount * 15);

        if (chance < threshold && base != Rarity.MYTHIC) {
            if (base == Rarity.RARE) return Rarity.ELITE;
            if (base == Rarity.ELITE) return Rarity.MYTHIC;
        }

        return base;
    }

    private static Map<String, Object> extractSensorProperties(Map<String, Object> sensorData) {
        Map<String, Object> props = new HashMap<>();
        if (sensorData != null) {
            for (Map.Entry<String, Object> entry : sensorData.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return props;
    }

    private static Entity generateRandomEntity(Rarity rarity, Map<String, Object> sensorData) {
        String baseName = generateBaseName(rarity);
        String flavorText = generateFlavorText(rarity, sensorData);
        String properties = generateProperties(rarity);

        return new Entity(baseName, rarity, flavorText, properties);
    }

    private static String generateBaseName(Rarity rarity) {
        switch (rarity) {
            case COMMON:
                return new String[]{"Shard", "Fragment", "Echo", "Pulse"}[RANDOM.nextInt(4)];
            case RARE:
                return new String[]{"Spirit", "Wisp", "Resonance", "Flux"}[RANDOM.nextInt(4)];
            case ELITE:
                return new String[]{"Nexus", "Guardian", "Wraith", "Construct"}[RANDOM.nextInt(4)];
            case MYTHIC:
                return new String[]{"Avatar", "Paragon", "Titan", "Ascendant"}[RANDOM.nextInt(4)];
            default:
                return "Unknown Entity";
        }
    }

    private static String generateFlavorText(Rarity rarity, Map<String, Object> sensorData) {
        String[] commonFlavors = {
                "A subtle presence fills the air.",
                "The sensors pick up echoes of something unseen.",
                "Data streams vibrate with energy.",
                "An unknown signature lingers."
        };

        String[] rareFlavors = {
                "Your device detects whispers of ethereal beings.",
                "The network pulses with living energy.",
                "Mysterious signals flow through the airwaves.",
                "Something watches through the digital medium."
        };

        String[] eliteFlavors = {
                "A powerful entity stirs in the wireless spectrum.",
                "Ancient intelligence resonates from the hardware.",
                "The veil is thin in this digital space.",
                "A guardian manifests from data patterns."
        };

        String[] mythicFlavors = {
                "A god awakens from the digital ether.",
                "Reality bends around this entity's presence.",
                "The universe reveals its hidden layers here.",
                "A transcendental force contaminates the signals."
        };

        String[] flavors;
        switch (rarity) {
            case COMMON:
                flavors = commonFlavors;
                break;
            case RARE:
                flavors = rareFlavors;
                break;
            case ELITE:
                flavors = eliteFlavors;
                break;
            case MYTHIC:
                flavors = mythicFlavors;
                break;
            default:
                flavors = commonFlavors;
        }

        Random random = new Random();
        if (sensorData != null && sensorData.containsKey("signal_count")) {
            return flavors[random.nextInt(flavors.length)] +
                    " Detected from " + sensorData.get("signal_count") + " signals.";
        }

        return flavors[random.nextInt(flavors.length)];
    }

    private static String generateProperties(Rarity rarity) {
        String[] properties = {
                "Physical: Unknown, Magical: Unknown, Resistance: Unknown",
                "Physical: Low, Magical: Moderate, Resistance: Moderate",
                "Physical: Moderate, Magical: High, Resistance: Moderate",
                "Physical: High, Magical: Very High, Resistance: High",
                "Physical: Very High, Magical: Extreme, Resistance: Very High"
        };

        return properties[rarity.ordinal()];
    }

    public static OUIDatabase getOUIDatabase() {
        return new OUIDatabase(OUI_BIAS_MAP);
    }
}