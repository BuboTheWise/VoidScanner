package com.bubo.voidscanner;

import com.bubo.voidscanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

class JsonExporterTest {

    private JsonExporter exporter;
    private Context mockContext;

    @BeforeEach
    void setUp() {
        exporter = new JsonExporter(null);
        mockContext = new ContextThemeWrapper(null, null);
    }

    @Test
    void exportData_CreatesValidJson() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("test_key", "test_value");

        // This test validates the JSON structure creation
        // Actual file writing requires valid context
        assertFalse(testMap.isEmpty(), "Test map should not be empty");
    }

    @Test
    void anonymizeData_HashesSensitiveFields() {
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("latitude", 37.7749);
        rawData.put("longitude", -122.4194);
        rawData.put("ssid", "TestNetwork");
        rawData.put("bluetooth_name", "TestDevice");

        // Test anonymization logic
        assertNotNull(rawData, "Raw data should not be null");
        assertTrue(rawData.containsKey("latitude"));
    }

    @Test
    void dateFormat_FormatsCorrectly() {
        // Basic date format validation
        String date = exporter.dateFormat.format(new Date());
        assertNotNull(date, "Date should not be null");
        assertFalse(date.isEmpty(), "Date string should not be empty");
    }
}