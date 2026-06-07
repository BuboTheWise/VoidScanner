package com.bubo.voidscanner;

import java.util.*;

/**
 * OUIDatabase - Maps MAC address OUI prefixes to entity biases.
 * Stores known device/vendor types and their associated entity preferences.
 * 
 * @version 1.2.0
 */
public class OUIDatabase {
    private static final Map<String, String> OUI_ENTITY_MAP = new LinkedHashMap<>();
    
    static {
        // Init with expanded list from Requirements.md section 2
        addMapping("00:17:88", "LUMINAR", "Philips Hue smart lighting");
        addMapping("FC:9C:98", "WATCHER", "Arlo security cameras");
        addMapping("00:0D:52", "WATCHER", "Arlo older security cameras");
        addMapping("18:B4:30", "TEMPISTRY", "Nest Google thermostats");
        addMapping("30:6F:07", "TEMPISTRY", "Ecobee thermostats");
        addMapping("00:1A:2A", "POWER_ELEM", "TP-Link smart plugs");
        addMapping("48:3F:DA", "POWER_ELEM", "TP-Link smart home");
        addMapping("24:9E:10", "WATCHER", "Ring doorbells/cameras");
        addMapping("B0:CE:18", "POWER_ELEM", "Belkin WeMo smart plugs");
        addMapping("00:1C:42", "AETHER_DRIFTER", "Apple devices");
        addMapping("00:1A:79", "ROBUST_EARTH", "Samsung devices");
        addMapping("84:9A:12", "POWER_ELEM", "Huawei IoT devices");
        addMapping("44:4A:B1", "NESTLE_DRONE", "Xiaomi smart home");
        addMapping("00:02:72", "WATCHER", "Netgear smart cameras");
        addMapping("00:0F:80", "POWER_ELEM", "D-Link IoT devices");
        addMapping("D4:0C:21", "AETHER_DRIFTER", "Nintendo Switch");
        addMapping("68:69:4E", "TECH_REVENANT", "Nvidia devices");
        addMapping("AC:09:7B", "WATCHER", "Logitech sensors");
    }
    
    public static List<String> getEntityBiases(String oui) {
        List<String> biases = new ArrayList<>();
        // Match exact prefix
        if (OUI_ENTITY_MAP.containsKey(oui)) {
            biases.add(OUI_ENTITY_MAP.get(oui));
        }
        // Match first 8 chars of MAC (OUI without colon)
        String ouiNoColons = oui.replace(":", "");
        for (Map.Entry<String, String> entry : OUI_ENTITY_MAP.entrySet()) {
            if (entry.getKey().replace(":", "").startsWith(ouiNoColons.substring(0, 6))) {
                biases.add(entry.getValue());
            }
        }
        return biases;
    }
    
    public static boolean isKnownOUI(String oui) {
        return OUI_ENTITY_MAP.containsKey(oui);
    }
    
    public static List<String> getAllOUIs() {
        return new ArrayList<>(OUI_ENTITY_MAP.keySet());
    }
    
    private static void addMapping(String oui, String entityBias, String note) {
        OUI_ENTITY_MAP.put(oui, entityBias);
    }
}
