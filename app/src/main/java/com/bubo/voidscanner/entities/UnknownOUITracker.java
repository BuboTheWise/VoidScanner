package com.bubo.voidscanner;

import java.util.*;

/**
 * UnknownOUITracker - Tracks unknown OUI signatures for future expansion.
 * Maintains frequency counts, average RSSI, and last seen timestamps.
 * 
 * @version 1.2.0
 */
public class UnknownOUITracker {
    
    private static final Map<String, OUIRecord> OUI_RECORDS = new LinkedHashMap<>();
    private static final int MAX_RECENT_OUIS = 100;
    
    /**
     * Record of unknown OUI with statistics
     */
    public static class OUIRecord {
        public final String oui;
        public final String mac;  // Sample MAC address
        public int count;         // How many times seen
        public double avgRssi;    // Average signal strength
        public long lastSeen;     // Timestamp (UTC epoch ms)
        public double signalStrengthTotal;
        
        public OUIRecord(String oui, String mac, double rssi) {
            this.oui = oui;
            this.mac = mac;
            this.count = 1;
            this.avgRssi = rssi;
            this.lastSeen = System.currentTimeMillis();
            this.signalStrengthTotal = rssi;
        }
        
        public void addSample(double rssi) {
            count++;
            signalStrengthTotal += rssi;
            avgRssi = signalStrengthTotal / count;
            lastSeen = System.currentTimeMillis();
        }
    }
    
    /**
     * Track an unknown OUI from scan result
     */
    public static void addOUI(String mac, double rssi) {
        String oui = EntityGenerator.getOUI(mac);
        
        if (oui == null) {
            return;
        }
        
        if (!OUI_RECORDS.containsKey(oui)) {
            OUI_RECORDS.put(oui, new OUIRecord(oui, mac, rssi));
        } else {
            OUI_RECORDS.get(oui).addSample(rssi);
        }
        
        // Trim old entries
        if (OUI_RECORDS.size() > MAX_RECENT_OUIS) {
            // Remove oldest ones (LinkedHashMap maintains insertion order)
            List<String> keys = new ArrayList<>(OUI_RECORDS.keySet());
            Collections.sort(keys, (a, b) -> Long.compare(OUI_RECORDS.get(a).lastSeen, OUI_RECORDS.get(b).lastSeen));
            
            int trimCount = OUI_RECORDS.size() - MAX_RECENT_OUIS;
            for (int i = 0; i < trimCount && i < keys.size(); i++) {
                OUI_RECORDS.remove(keys.get(i));
            }
        }
    }
    
    /**
     * Get top N unknown OUIs by frequency (descending)
     */
    public static List<OUIRecord> getTopUnknownOUIs(int n) {
        List<OUIRecord> records = new ArrayList<>(OUI_RECORDS.values());
        
        // Sort by count (descending), then avgRssi (descending), then lastSeen
        records.sort((a, b) -> {
            if (b.count != a.count) {
                return Integer.compare(b.count, a.count);
            }
            if (b.avgRssi != a.avgRssi) {
                return Double.compare(b.avgRssi, a.avgRssi);
            }
            return Long.compare(a.lastSeen, b.lastSeen);
        });
        
        return records.subList(0, Math.min(n, records.size()));
    }
    
    /**
     * Get count of all unknown OUIs
     */
    public static int getTotalUnknownOUICount() {
        return OUI_RECORDS.size();
    }
    
    /**
     * Check if OUI is unknown (not in OUIDatabase)
     */
    public static boolean isUnknownOUI(String oui) {
        return !OUIDatabase.isKnownOUI(oui);
    }
    
    /**
     * Get all unknown OUIs
     */
    public static List<String> getAllUnknownOUIs() {
        List<String> unknown = new ArrayList<>();
        for (String oui : OUIDatabase.getAllOUIs()) {
            if (isUnknownOUI(oui)) {
                unknown.add(oui);
            }
        }
        return unknown;
    }
    
    /**
     * Clear all tracked OUIs
     */
    public static void clear() {
        OUI_RECORDS.clear();
    }
    
    /**
     * Export formatted summary of unknown OUIs
     */
    public static String exportSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Unknown OUI Summary ===\n");
        sb.append(String.format("Total Unknown Signatures: %d\n", getTotalUnknownOUICount()));
        sb.append("\nTop Unknown OUIs:\n");
        
        List<OUIRecord> top = getTopUnknownOUIs(5);
        if (top.isEmpty()) {
            sb.append("(none)\n");
        } else {
            for (int i = 0; i < top.size(); i++) {
                OUIRecord record = top.get(i);
                sb.append(String.format("%d. %s\t×%d\tavgRssi=%.1f dBm\n",
                        i + 1, record.oui, record.count, record.avgRssi));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Get known OUI count (for comparison)
     */
    public static int getKnownOUICount() {
        return OUIDatabase.getAllOUIs().size();
    }
}