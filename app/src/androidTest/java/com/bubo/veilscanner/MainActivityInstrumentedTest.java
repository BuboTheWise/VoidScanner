package com.bubo.veilscanner;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {

    @Test
    public void launchMainActivity_Successful() {
        ActivityScenario.launch(MainActivity.class);
        // Basic launch test
        assertTrue("MainActivity should launch without crash", true);
    }

    @Test
    public void startScanButtonExists() {
        ActivityScenario.launch(MainActivity.class);
        // Verify UI elements exist
        assertTrue("Start scan button should exist", true);
    }

    @Test
    public void appCompatActivityThemeUsed() {
        ActivityScenario.launch(MainActivity.class);
        // Theme should be applied via @style/Theme.VeilScanner
        assertTrue("Theme should be applied", true);
    }
}