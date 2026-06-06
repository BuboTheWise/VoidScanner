package com.bubo.voidscanner.entities;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages OUI (Organizationally Unique Identifier) database and unknown OUI tracking
 * Following [[Requirements.md]] section 2: OUI/Vendor Intelligence & Entity Mapping
 */
public class OUIDatabase {
    private final Map<String, Entity> knownOUIs;
    private final Map<String, OUIStats> unknownOUIStats;

    public OUIDatabase() {
        this.knownOUIs = new HashMap<>();
        this.unknownOUIStats = new ConcurrentHashMap<>();
    }

    public OUIDatabase(Map<String, Entity> initialOIUs) {
        this.knownOUIs = new HashMap<>(initialOIUs);
        this.unknownOUIStats = new ConcurrentHashMap<>();
    }

    /**
     * Register a known OUI mapping following [[Requirements.md]] section 2
     *
     * @param oui OUI address (e.g., "A4:83:E7")
     * @param vendor Vendor name
     * @param entity Associated entity for bias
     */
    public void registerKnownOUI(String oui, String vendor, Entity entity) {
        String formattedOUI = normalizeOUI(oui);
        knownOUIs.put(formattedOUI, entity);
    }

    /**
     * Record detection of unknown/unclassified OUI
     *
     * @param oui OUI detected
     * @param rssi RSSI value in dBm
     */
    public void recordUnknownOUI(String oui, int rssi) {
        String formattedOUI = normalizeOUI(oui);
        unknownOUIStats.computeIfAbsent(formattedOUI, k -> new OUIStats())
                .addDetection(rssi);
    }

    /**
     * Retrieve entity bias for known OUI
     *
     * @param oui OUI to look up
     * @return Entity if found, null if unknown OUI
     */
    public Entity getEntityForOUI(String oui) {
        return knownOUIs.get(normalizeOUI(oui));
    }

    /**
     * Check if OUI is in known database
     */
    public boolean isKnownOUI(String oui) {
        return knownOUIs.containsKey(normalizeOUI(oui));
    }

    /**
     * Get most frequent unknown OUI
     *
     * @param limit Maximum number to return
     * @return List of top unknown OUIs with stats
     */
    public List<OUIStats> getTopUnknownOUIs(int limit) {
        return unknownOUIStats.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().getAverageRSSI() - e1.getValue().getAverageRSSI())
                .limit(limit)
                .map(Map.Entry::getValue)
                .toList();
    }

    /**
     * Export unknown OUI list to Bubo_Wisdom vault for [[EntityGenerator]] expansion
     * Follows [[WORKFLOW-VAULT-CONVENTION]] for markdown output
     *
     * @return Markdown-formatted output for vault entry
     */
    public String exportUnknownOUIsToVault() {
        if (unknownOUIStats.isEmpty()) {
            return "# Unknown OUI Report\n\nNo unknown OUIs detected during scans.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# Unknown OUI Report\n\n");
        sb.append("These unknown device signatures have been detected and will be used to expand [[EntityGenerator]] bias table in future versions.\n\n");

        sb.append("| OUI | Detected Times | Average RSSI (dBm) | Average RSSI | Status |\n");
        sb.append("|-----|----------------|-------------------|--------------|--------|\n");

        List<OUIStats> topUnknowns = getTopUnknownOUIs(10);
        for (OUIStats stats : topUnknowns) {
            sb.append(String.format("| %s | %d | %d | %f | Pending Expansion |\n",
                    stats.getOui(),
                    stats.getDetectionCount(),
                    stats.getAverageRSSI(),
                    stats.getAverageRSSI()
            ));
        }

        sb.append("\n## Recommended Actions\n\n");
        sb.append("1. Verify these OUIs in real-world scanning\n");
        sb.append("2. Update [[EntityGenerator]] OUI_BIAS_MAP with confirmed vendor mappings\n");
        sb.append("3. Add flavor text for each new vendor-entity pair\n");
        sb.append("4. Test entity generation with new OUIs\n");

        return sb.toString();
    }

    public Map<String, Entity> getKnownOUIs() {
        return Collections.unmodifiableMap(knownOUIs);
    }

    public Map<String, OUIStats> getUnknownOUIStats() {
        return Collections.unmodifiableMap(unknownOUIStats);
    }

    /**
     * OUI statistics class for tracking unknown signatures
     */
    public static class OUIStats {
        private final String oui;
        private int totalRSSI = 0;
        private int detectionCount = 0;
        private long lastSeen = 0;

        public OUIStats() {
            this.oui = "";
        }

        public OUIStats(String oui) {
            this.oui = oui;
        }

        public void addDetection(int rssi) {
            totalRSSI += rssi;
            detectionCount++;
            lastSeen = System.currentTimeMillis();
        }

        public double getAverageRSSI() {
            return detectionCount == 0 ? 0 : (double) totalRSSI / detectionCount;
        }

        public String getOui() {
            return oui;
        }

        public int getDetectionCount() {
            return detectionCount;
        }

        public long getLastSeen() {
            return lastSeen;
        }

        @Override
        public String toString() {
            return String.format("OUIStats{oui='%s', count=%d, avg_rssi=%.2f}",
                    oui, detectionCount, getAverageRSSI());
        }
    }

    private String normalizeOUI(String oui) {
        // Convert to uppercase and remove colons: "A4:83:E7" -> "A483E7"
        return oui.trim().toUpperCase().replace(":", "");
    }

    @Override
    public String toString() {
        return String.format("OUIDatabase{known=%d, unknown=%d}",
                knownOUIs.size(), unknownOUIStats.size());
    }
}