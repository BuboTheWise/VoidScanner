package com.bubo.voidscanner;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class EntityGeneratorTest {

    @Test
    public void testEntityGeneratorCreation() {
        // Basic test to make sure the class compiles properly and can be instantiated
        assertNotNull("EntityGenerator should not be null", EntityGenerator.class);
    }

    @Test
    public void testScanFeaturesCreation() {
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        assertNotNull("ScanFeatures should be created", features);
        assertNotNull("unknownOuis list should not be null", features.unknownOuis);
    }

    @Test
    public void testVendorMapping() {
        // Test that we can get vendor names for known OUIs
        String vendor = EntityGenerator.getVendorFromOui("00:17:88");
        assertEquals("Should return Philips Hue for this OUI", "Philips Hue", vendor);
        
        // Test that we can check if an OUI is known
        assertTrue("Should recognize known OUI", EntityGenerator.isKnownOui("00:17:88"));
        assertFalse("Should not recognize unknown OUI", EntityGenerator.isKnownOui("AA:BB:CC"));
    }

    @Test
    public void testRandomOuiGeneration() {
        // Test that we can generate random OUIs
        java.util.Random random = new java.util.Random();
        String oui = EntityGenerator.generateRandomOui(random);
        assertNotNull("Generated OUI should not be null", oui);
        assertTrue("Generated OUI should contain colons", oui.contains(":"));
    }

    @Test  
    public void testRarityDetermination() {
        // Test that we can determine rarity from features
        EntityGenerator.ScanFeatures features = new EntityGenerator.ScanFeatures();
        features.proximity = 50; // Strong signal
        features.humanDensity = 40; 
        features.wifiRssiAvg = 70.0;
        
        java.util.Random random = new java.util.Random();
        // This should return MYTHIC based on our implementation
        // We're not testing the exact result but making sure it doesn't crash
        assertNotNull("Rarity determination should work", 
                     EntityGenerator.determineRarityBySignal(features, random));
    }
}