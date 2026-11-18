package com.bubo.voidscanner.entities;

import org.junit.Test;
import static org.junit.Assert.*;

public class EntityTest {

    @Test
    public void testEntityCreationCommon() {
        Entity entity = new Entity("TestEntity", Rarity.COMMON, "A test entity", "TestProp");
        assertEquals("TestEntity", entity.getName());
        assertEquals(Rarity.COMMON, entity.getRarity());
    }

    @Test
    public void testEntityCreationRare() {
        Entity entity = new Entity("RareEntity", Rarity.RARE, "A rare entity", "RareProp");
        assertEquals("RareEntity", entity.getName());
        assertEquals(Rarity.RARE, entity.getRarity());
    }

    @Test
    public void testEntityCreationElite() {
        Entity entity = new Entity("EliteEntity", Rarity.ELITE, "An elite entity", "EliteProp");
        assertEquals(Rarity.ELITE, entity.getRarity());
    }

    @Test
    public void testEntityCreationMythic() {
        Entity entity = new Entity("MythicEntity", Rarity.MYTHIC, "A mythic entity", "MythicProp");
        assertEquals(Rarity.MYTHIC, entity.getRarity());
    }

    @Test
    public void testToStringCommon() {
        Entity entity = new Entity("Peasant", Rarity.COMMON, "A simple person", "Weak");
        String output = entity.toString();
        assertTrue(output.contains("Peasant"));
        assertTrue(output.contains("COMMON"));
        assertTrue(output.contains("1, 2, 2"));
    }

    @Test
    public void testToStringRare() {
        Entity entity = new Entity("Ghost", Rarity.RARE, "Ethereal being", "Invisible");
        String output = entity.toString();
        assertTrue(output.contains("Ghost"));
        assertTrue(output.contains("RARE"));
        assertTrue(output.contains("3, 4, 3"));
    }

    @Test
    public void testToStringElite() {
        Entity entity = new Entity("Dragon", Rarity.ELITE, "Ancient power", "Dragon power");
        assertTrue(entity.toString().contains("6, 7, 6"));
    }

    @Test
    public void testToStringMythic() {
        Entity entity = new Entity("God", Rarity.MYTHIC, "Omnipotent being", "Power beyond mortal understanding");
        assertTrue(entity.toString().contains("MYTHIC"));
        assertTrue(entity.toString().contains("9, 10, 9"));
    }

    @Test
    public void testToJSON() {
        Entity entity = new Entity("Spell", Rarity.MYTHIC, "Burning hot", "Immune to cold");
        String json = entity.toJSON();
        assertTrue(json.contains("\"name\":\"Spell\""));
        assertTrue(json.contains("\"rarity\":\"mythic\""));
        assertTrue(json.contains("\"flavor\":\"Burning hot\""));
        assertTrue(json.contains("\"properties\":\"Immune to cold\""));
    }

    @Test
    public void testFlavorText() {
        Entity entity = new Entity("Spell", Rarity.RARE, "Burning hot", "Properties");
        assertEquals("Burning hot", entity.getFlavorText());
        assertEquals("Properties", entity.getProperties());
    }

    @Test
    public void testEntityNotNull() {
        assertNotNull(new Entity("X", Rarity.COMMON, "Y", "Z"));
    }

    @Test
    public void testMultipleInstances() {
        Entity e1 = new Entity("X", Rarity.COMMON, "Y", "Z");
        Entity e2 = new Entity("X", Rarity.COMMON, "Y", "Z");
        assertNotSame(e1, e2);
    }

    @Test
    public void testRarityEnum() {
        assertEquals(4, Rarity.values().length);
        String[] expected = {"COMMON", "RARE", "ELITE", "MYTHIC"};
        int i = 0;
        for (Rarity r : Rarity.values()) {
            assertTrue(r.toString(), r.toString().equals(expected[i]));
            i++;
        }
    }
}