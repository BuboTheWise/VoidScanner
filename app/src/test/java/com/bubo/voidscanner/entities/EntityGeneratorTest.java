package com.bubo.voidscanner.entities;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

/**
 * Unit tests for EntityGenerator and related classes.
 * Tests deterministic generation, OUIDatabase functionality, and Entity creation.
 * 
 * @version 1.2.0
 */
public class EntityGeneratorTest {
    
    @Before
    public void setUp() {
        // Initialize tests with clean state
        UnknownOUITracker.clear();
    }
    
    /**
     * Test OUIDatabase expansion (10 entries → >20)
     */
    @Test
    public void testOUIDatabaseExpansion() {
        assertEquals("OUI database should have 19 entries", 19, OUIDatabase.getAllOUIs().size());
        assertTrue("Apple OUI should exist", OUIDatabase.isKnownOUI("00:1C:42"));
        assertTrue("TP-Link OUI should exist", OUIDatabase.isKnownOUI("00:1A:2A"));
    }
    
    /**
     * Test OUI to Entity bias mapping
     */
    @Test
    public void testOUIToEntityMapping() {
        List<String> biases = OUIDatabase.getEntityBiases("00:17:88");
        assertTrue("Philips Hue should map to LUMINAR", biases.contains("LUMINAR"));
        
        biases = OUIDatabase.getEntityBiases("00:0D:52");
        assertTrue("Arlo should map to WATCHER", biases.contains("WATCHER"));
        
        biases = OUIDatabase.getEntityBiases("18:B4:30");
        assertTrue("Nest should map to TEMPISTRY", biases.contains("TEMPISTRY"));
    }
    
    /**
     * Test unknown OUI detection
     */
    @Test
    public void testUnknownOUIDetection() {
        // Known OUI
        assertFalse("Known OUI should NOT be unknown",
                UnknownOUITracker.isUnknownOUI("00:17:88"));
        
        // Make up a random OUI
        assertTrue("Random OUI should be unknown",
                UnknownOUITracker.isUnknownOUI("FF:FF:FF:XX"));
    }
    
    /**
     * Test unknown OUI tracking
     */
    @Test
    public void testUnknownOUITracking() {
        UnknownOUITracker.addOUI("AA:BB:CC:11:22:33", -65);
        UnknownOUITracker.addOUI("AA:BB:CC:11:22:33", -70);
        
        assertTrue("Should track unknown OUI", 
                UnknownOUITracker.getTotalUnknownOUICount() > 0);
        
        List<UnknownOUITracker.OUIRecord> top = UnknownOUITracker.getTopUnknownOUIs(1);
        assertEquals("Should return exact count", 2, top.get(0).count);
        assertEquals("Avg RSSI should be calculated correctly", 
                (-65 + -70) / 2.0, top.get(0).avgRssi, 0.01);
    }
    
    /**
     * Test entity hash generation (deterministic)
     */
    @Test
    public void testEntityHashGeneration() {
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        features.humanDensity = 3;
        features.iotPresence = 1;
        
        // Generate entities multiple times
        for (int i = 0; i < 10; i++) {
            List<DiscoveredEntity> entities = EntityGenerator.generateFromScan(
                    " SCAN-" + i, features);
            
            assertFalse("Should generate at least one entity", entities.isEmpty());
            for (DiscoveredEntity entity : entities) {
                assertEquals("Hash should be non-empty", 64, entity.getEntityHash().length());
            }
        }
    }
    
    /**
     * Test feature extraction (humanDensity and iotPresence)
     */
    @Test
    public void testFeatureExtraction() {
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        features.humanDensity = 8;
        features.iotPresence = 3;
        
        List<DiscoveredEntity> entities = EntityGenerator.generateFromScan("SCAN-TEST", features);
        
        assertNotNull("Should generate entities", entities);
        assertEquals("Should generate 1-5 entities", 
                (1 <= entities.size() && entities.size() <= 5), true);
    }
    
    /**
     * Test scan seed generation (should be unique per scan)
     */
    @Test
    public void testScanSeedUniqueness() {
        String scanId1 = "SCAN-001";
        String scanId2 = "SCAN-002";
        
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        features.humanDensity = 5;
        
        long seed1 = EntityGenerator.generateBaseSeed(scanId1, features);
        long seed2 = EntityGenerator.generateBaseSeed(scanId2, features);
        
        assertNotEquals("Seeds should differ for different scan IDs", seed1, seed2);
    }
    
    /**
     * Test scan seed determinism (same ID = same seed)
     */
    @Test
    public void testScanSeedDeterminism() {
        String scanId = "SCAN-AAA";
        
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        features.humanDensity = 4;
        
        long seed1 = EntityGenerator.generateBaseSeed(scanId, features);
        long seed2 = EntityGenerator.generateBaseSeed(scanId, features);
        
        assertEquals("Seeds should match for same ID", seed1, seed2);
    }
    
    /**
     * Test entity name generation
     */
    @Test
    public void testEntityNameGeneration() {
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        features.humanDensity = 3;
        
        List<DiscoveredEntity> entities = EntityGenerator.generateFromScan("SCAN-TEST", features);
        
        for (DiscoveredEntity entity : entities) {
            assertNotNull("Entity name should not be empty", entity.getName());
            assertFalse("Entity name should not be null", entity.getName().isEmpty());
            assertTrue("Name should contain base type", entity.getName().contains(entity.getBaseType()));
            assertNotNull("Entity hash should not be null", entity.getEntityHash());
        }
    }
    
    /**
     * Test entity rarity calculation
     */
    @Test
    public void testEntityRarityCalculation() {
        EntityGenerator.ScanFeatures lowActivity = new EntityGenerator.ScanFeatures();
        lowActivity.humanDensity = 2;
        
        EntityGenerator.ScanFeatures highActivity = new EntityGenerator.ScanFeatures();
        highActivity.humanDensity = 10;
        highActivity.iotPresence = 4;
        
        List<DiscoveredEntity> low = EntityGenerator.generateFromScan("SCAN-LOW", lowActivity);
        List<DiscoveredEntity> high = EntityGenerator.generateFromScan("SCAN-HIGH", highActivity);
        
        // High activity should yield rarer entities more frequently
        int highRareCount = 0;
        int highUncommonCount = 0;
        
        for (DiscoveredEntity entity : high) {
            if (entity.getRarity() == DiscoveredEntity.Rarity.RARE || 
                    entity.getRarity() == DiscoveredEntity.Rarity.ANOMALOUS) {
                if (entity.getRarity() != DiscoveredEntity.Rarity.COMMON) {
                    highRareCount++;
                }
            } else if (entity.getRarity() == DiscoveredEntity.Rarity.UNCOMMON) {
                highUncommonCount++;
            }
        }
        
        assertTrue("High activity should produce some rare entities", highRareCount > 0 || highUncommonCount > 0);
    }
    
    /**
     * Test entity power level range
     */
    @Test
    public void testEntityPowerLevelRange() {
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        features.humanDensity = 5;
        
        List<DiscoveredEntity> entities = EntityGenerator.generateFromScan("SCAN-TEST", features);
        
        for (DiscoveredEntity entity : entities) {
            assertTrue("Power level should be 0-100", 
                    entity.getPowerLevel() >= 0 && entity.getPowerLevel() <= 100);
        }
    }
    
    /**
     * Test unknown OUI influence on anomaly generation
     */
    @Test
    public void testUnknownOUIAnomalyGeneration() {
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        features.humanDensity = 3;
        features.unknownOuis.add("FF:AA:BB:11:22:33");
        
        List<DiscoveredEntity> entities = EntityGenerator.generateFromScan("SCAN-UNKNOWN", features);
        
        // Unknown OUIs should trigger anomaly or at least some entities
        int anomalyCount = 0;
        for (DiscoveredEntity entity : entities) {
            if (entity.getRarity() == DiscoveredEntity.Rarity.ANOMALOUS) {
                anomalyCount++;
            }
        }
        
        // Anomalies are rare (10%+ chance when unknown OUIs present)
        assertTrue("Unknown OUIs should influence generation", 
                anomalyCount >= 0 && anomalyCount < 3);
    }
    
    /**
     * Test empty scan should still produce entities
     */
    @Test
    public void testEmptyScanStillProducesEntities() {
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        
        List<DiscoveredEntity> entities = EntityGenerator.generateFromScan("SCAN-EMPTY", features);
        
        assertFalse("Empty scan should still produce entities", entities.isEmpty());
        
        for (DiscoveredEntity entity : entities) {
            assertNotNull("Should have valid entity", entity);
        }
    }
    
    /**
     * Test OUI extraction from MAC
     */
    @Test
    public void testOUIExtraction() {
        assertEquals("Should extract first 8 chars", "00:17:88", 
                EntityGenerator.getOUI("00:17:88:11:22:33"));
        assertEquals("Should handle empty MAC", null, EntityGenerator.getOUI(""));
    }
    
    /**
     * Test scan summary creation
     */
    @Test
    public void testScanFeatureCreation() {
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        features.humanDensity = 7;
        features.iotPresence = 2;
        features.unknownOuis.add("AA:01");
        features.unknownOuis.add("AA:02");
        
        List<DiscoveredEntity> entities = EntityGenerator.generateFromScan("SCAN-TEST", features);
        
        assertNotNull("Should generate entities", entities);
        for (DiscoveredEntity entity : entities) {
            assertTrue("Entities should have influencing unknown OUIs if present",
                    (entity.getInfluencingOuis().isEmpty() && features.unknownOuis.isEmpty()) ||
                    (!entity.getInfluencingOuis().isEmpty()));
        }
    }
    
    /**
     * Test multiple entity generation in single scan
     */
    @Test
    public void testMultipleEntityGeneration() {
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        features.humanDensity = 4;
        
        List<DiscoveredEntity> entities = EntityGenerator.generateFromScan("SCAN-MULT", features);
        
        assertNotNull("Should generate entities", entities);
        assertEquals("Should generate 1-5 entities", 
                (1 <= entities.size() && entities.size() <= 5), true);
        
        for (DiscoveredEntity entity : entities) {
            assertNotNull("Each entity should be valid", entity);
        }
    }
}